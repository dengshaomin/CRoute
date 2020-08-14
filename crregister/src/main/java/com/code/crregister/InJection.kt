package com.code.crregister

import jdk.internal.org.objectweb.asm.Opcodes
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 *  author : balance
 *  date : 2020/8/13 9:41 PM
 *  description :
 */
class InJection {
    companion object {
        fun inject(poolPath: String, poolName: String, components: MutableList<String>) {
            if (poolPath.isNullOrEmpty() || poolName.isNullOrEmpty() || components.isNullOrEmpty()) {
                return
            }
            if (poolPath.endsWith(".jar")) {
                injectJar(poolPath, poolName)
            } else {
                injectDirectory(poolPath)
            }
        }

        private fun injectJar(path: String, name: String) {
            var jarFile = File(path)
            var tempJar = File(jarFile.getParent(), jarFile.name + ".temp")
            if (tempJar.exists())
                tempJar.delete()
            val file = JarFile(jarFile)
            var enumeration = file.entries()
            var jarOutputStream = JarOutputStream(FileOutputStream(tempJar))

            while (enumeration.hasMoreElements()) {
                var jarEntry = enumeration.nextElement()
                var entryName = jarEntry.getName()
                var zipEntry = ZipEntry(entryName)
                var inputStream = file.getInputStream(jarEntry)
                jarOutputStream.putNextEntry(zipEntry)
                jarOutputStream.write(
                    if (entryName.contains(name)) injectCode(inputStream) else IOUtils.toByteArray(
                        inputStream
                    )
                )
                inputStream.close()
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            file.close()

            if (jarFile.exists()) {
                jarFile.delete()
            }
            tempJar.renameTo(jarFile)
        }

        private fun injectDirectory(path: String) {
            var file = File(path)
            var tempClass = File(file.getParent(), file.name + ".temp")
            var inputStream = FileInputStream(file)
            var outputStream = FileOutputStream(tempClass)
            outputStream.write(injectCode(inputStream))
            inputStream.close()
            outputStream.close()
            if (file.exists()) {
                file.delete()
            }
            tempClass.renameTo(file)
        }

        private fun injectCode(inputStream: InputStream): ByteArray {
            var cr = ClassReader(inputStream)
            var cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            var cv = ComponentCV(Opcodes.ASM5, cw)
            cr.accept(cv, ClassReader.EXPAND_FRAMES)
            return cw.toByteArray()
        }
    }


}