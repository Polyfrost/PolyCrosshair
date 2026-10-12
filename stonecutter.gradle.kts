plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3" /* [SC] DO NOT EDIT */

stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    constants["release"] = property("mod.id") != "template"

    replacements.string(eval(current.version, ">=1.21.6")) {
        replace("pushPose", "pushMatrix")
        replace("popPose", "popMatrix")
    }

    replacements.string(eval(current.version, "<1.21.11")) {
        replace("net.minecraft.Util", "net.minecraft.util.Util")
    }

    replacements.string(eval(current.version, ">=26")) {
        replace("net.minecraft.client.gui.GuiGraphics", "net.minecraft.client.gui.GuiGraphicsExtractor")
        replace("GuiGraphics", "GuiGraphicsExtractor")
    }

    // 26.3 moved the low-level GPU abstraction out of blaze3d into RenderPearl.
    replacements.string(eval(current.version, ">=26.3")) {
        replace("com.mojang.blaze3d.buffers", "com.mojang.renderpearl.api.buffers")
        replace("com.mojang.blaze3d.textures", "com.mojang.renderpearl.api.textures")
        replace("com.mojang.blaze3d.pipeline", "com.mojang.renderpearl.api.pipeline")
        replace("com/mojang/blaze3d/pipeline", "com/mojang/renderpearl/api/pipeline")
        replace("com.mojang.blaze3d.GpuFormat", "com.mojang.renderpearl.api.GpuFormat")
        replace("com.mojang.blaze3d.vertex.VertexFormat", "com.mojang.renderpearl.api.vertex.VertexFormat")
        replace("com.mojang.blaze3d.systems.GpuDevice", "com.mojang.renderpearl.api.device.GpuDevice")
        replace("com.mojang.blaze3d.systems.CommandEncoder", "com.mojang.renderpearl.api.commands.CommandEncoder")
    }
}

stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}
