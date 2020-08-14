package com.code.crregister

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 *  author : balance
 *  date : 2020/8/13 10:12 PM
 *  description :
 */
class ComponentCV(api: Int, cv: ClassVisitor?) : ClassVisitor(api, cv), Opcodes {

    override fun visitMethod(
        access: Int,
        name: String,
        desc: String?,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor? {
        val mv: MethodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
         if ("<clinit>".equals(name)) {
            return ComponentMV(api, mv)
        }
        return mv
    }

    override fun visitEnd() {
        super.visitEnd()
    }
}