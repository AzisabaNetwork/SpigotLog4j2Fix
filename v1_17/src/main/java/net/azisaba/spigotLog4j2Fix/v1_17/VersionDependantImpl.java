package net.azisaba.spigotLog4j2Fix.v1_17;

import io.netty.util.concurrent.GenericFutureListener;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import net.azisaba.spigotLog4j2Fix.common.VersionDependant;
import net.azisaba.spigotLog4j2Fix.common.util.Util;
import net.azisaba.spigotLog4j2Fix.v1_17.util.VersionUtil;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;

public class VersionDependantImpl implements VersionDependant {
    @Override
    public void register() throws Exception {
        CtMethod methodProcessPacket = Util.getCtMethod(VersionUtil.class.getMethod("processOutgoingPacket", Packet.class));
        Util.transformClass("net.minecraft.network.NetworkManager", cc -> {
            CtMethod nm = new CtMethod(CtClass.booleanType, "processOutgoingPacket", new CtClass[]{ ClassPool.getDefault().get("net.minecraft.network.protocol.Packet") }, cc);
            nm.setBody(methodProcessPacket, null);
            cc.addMethod(nm);
            CtMethod m = cc.getMethod("a", Util.buildBytecodeSignature(void.class, Packet.class, GenericFutureListener.class, EnumProtocol.class, EnumProtocol.class));
            m.insertBefore("{ System.err.println(org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(packet)); if (processOutgoingPacket($1)) { return; } }");
            return cc.toBytecode();
        });
        NetworkManager.class.getClassLoader();
    }
}
