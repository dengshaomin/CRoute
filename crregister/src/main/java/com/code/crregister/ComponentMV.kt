package com.code.crregister

import com.code.crregister.CRoutePlugin.Companion.components
import com.code.crregister.CRoutePlugin.Companion.poolClassName
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 *  author : balance
 *  date : 2020/8/13 9:56 PM
 *  description :
 */
class ComponentMV(api: Int, mv: MethodVisitor?) : MethodVisitor(api, mv) {
    override fun visitCode() {
        super.visitCode()
    }

    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.RETURN && !components.isNullOrEmpty()) {
            for (key in components) {
                mv.visitLdcInsn(key)
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    poolClassName,
                    "registerComponent",
                    "(Ljava/lang/String;)V",
                    false
                )
            }
        }
        super.visitInsn(opcode)
    }
}