package com.code.crregister

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.runtime.IOGroovyMethods
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.ASM5
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 *  author : balance
 *  date : 2020/8/13 11:18 AM
 *  description :
 */
class CRoutePlugin : Transform(), Plugin<Project> {
    companion object {
        //需要扫描的IRoute完整类名
        val scanInterfaceName = "com/code/croutefactory/IRoute"
        //需要扫描组件池RoutePool完整类名
        var poolClassName = "com/code/croutefactory/RoutePoolJava"
        //扫描到的组件
        val components: MutableList<String> = mutableListOf()
        //组件池所在的目标路径
        var poolClassPath: String? = null
        //扫描过的缓存列表
        var cacheList = mutableListOf<String>()
    }

    override fun getName(): String {
        return CRoutePlugin::class.java.name
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun apply(p0: Project) {
        val android = p0.extensions.getByType(AppExtension::class.java)
        //注入transform
        android.registerTransform(this)
    }

    override fun transform(transformInvocation: TransformInvocation?) {
//        super.transform(transformInvocation)
        println("********" + this@CRoutePlugin.javaClass.simpleName + "  start")
        var isIncremental = transformInvocation?.isIncremental()
        if (!isIncremental!!) {
            //如果不是增量编译，清除所有数据
            transformInvocation?.outputProvider?.deleteAll()
            components.clear()
            cacheList.clear()
        }
        var startTime = System.currentTimeMillis()
        transformInvocation?.inputs?.forEach {
            //扫描所有jar
            it.jarInputs.forEach {
                var path = it.file.absolutePath
                when (it.status) {
                    Status.NOTCHANGED -> {
                        //当jar没有变更且缓存中有该路径，跳过扫描
                        if (cacheList.contains(path)) {
                            return@forEach
                        } else {
                            cacheList.add(path)
                        }
                    }
                    Status.REMOVED -> {
                        //当jar被移除时，移除扫描数据
                        it.file.delete()
                        if (cacheList.contains(path)) {
                            cacheList.remove(path)
                        }
                        return@forEach
                    }
                }
                if(!cacheList.contains(path)){
                    cacheList.add(path)
                }
                scanJar(it, transformInvocation.outputProvider)
            }
            //扫描所有目录
            it.directoryInputs.forEach {
                scanDirectory(it, transformInvocation.outputProvider)
            }
        }

        println("isIncremental:" + isIncremental)
        println("components:" + components)
        println("pool path:" + poolClassPath)
        poolClassPath?.let {
            //向组件池内注入字节码
            InJection.inject(it, poolClassName, components) }
        println("inject finish const:" + (System.currentTimeMillis() - startTime))
        println("********" + this@CRoutePlugin.javaClass.simpleName + "  end")
    }

    fun scanJar(jarInput: JarInput, outputProvider: TransformOutputProvider) {
        if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
            var jarName = jarInput.name
            var md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length - 4)
            }
            var jarFile = JarFile(jarInput.file)
            var enumeration = jarFile.entries()
            var tmpFile = File(jarInput.file.getParent() + File.separator + "classes_temp.jar")
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
            var dest = outputProvider.getContentLocation(
                jarName + md5Name,
                jarInput.contentTypes, jarInput.scopes, Format.JAR
            )

            var jarOutputStream = JarOutputStream(FileOutputStream(tmpFile))
            while (enumeration.hasMoreElements()) {
                var jarEntry = enumeration.nextElement()
                var entryName = jarEntry.getName()
                var zipEntry = ZipEntry(entryName)
                var inputStream = jarFile.getInputStream(jarEntry)
                var bytes = IOUtils.toByteArray(inputStream)
                if (!components.contains(entryName) && checkClassFile(entryName)) {
                    if (entryName.contains(poolClassName)) {
                        //找到组件池所在的jar
                        poolClassPath = dest.path
                    }
                    var classReader = ClassReader(bytes)
                    if (classReader.interfaces.contains(scanInterfaceName)) {
                        //找到继承了IRoute的组件入口
                        components.add(entryName)
                    }
                }
                jarOutputStream.putNextEntry(zipEntry)
                jarOutputStream.write(bytes)
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            jarFile.close()
            FileUtils.copyFile(tmpFile, dest)
            tmpFile.delete()
        }

    }

    fun scanDirectory(input: DirectoryInput, outputProvider: TransformOutputProvider) {
        var dest = outputProvider.getContentLocation(
            input.name,
            input.contentTypes,
            input.scopes,
            Format.DIRECTORY
        )
        if (input.file.isDirectory()) {
            input.file.walk().iterator().forEach {

                if (!components.contains(it.name) && checkClassFile(it.name)) {
                    var classReader = ClassReader(it.readBytes())
                    if (classReader.interfaces.contains(scanInterfaceName)) {
                        if (!components.contains(it.name)) {
                            //找到继承了IRoute的组件入口
                            components.add(it.name)
                        }
                    }
                    if (it.name.contains(poolClassName)) {
                        //找到组件池所在的目录
                        poolClassPath = dest.absolutePath
                    }
                }
            }
        }
        FileUtils.copyDirectory(input.file, dest)
    }
    //过滤不需要扫描的类，提升速度
    fun checkClassFile(name: String): Boolean {
        return name.endsWith(".class")  //必须是class文件
                && !name.startsWith("android") //过滤android/androidx系统库
                && !name.startsWith("kotlin")    //过滤kotlin/kotlinx系统库
                && !name.startsWith("org/jetbrains") //过滤jetbrains
                && !name.startsWith("org/intellij")  //过滤intellij
                && !name.contains("\$")    //过滤资源相关文件
                && !name.endsWith("R.class") //过滤R
                && !name.contains("BuildConfig.class")//过滤 buildconfig
    }
}