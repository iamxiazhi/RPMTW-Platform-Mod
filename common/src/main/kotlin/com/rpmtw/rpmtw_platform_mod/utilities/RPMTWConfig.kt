package com.rpmtw.rpmtw_platform_mod.utilities

import com.mojang.blaze3d.vertex.PoseStack
import com.rpmtw.rpmtw_platform_mod.RPMTWPlatformMod
import com.rpmtw.rpmtw_platform_mod.RPMTWPlatformModPlugin
import com.rpmtw.rpmtw_platform_mod.gui.ConfigScreen
import com.rpmtw.rpmtw_platform_mod.handlers.RPMTWAuthHandler
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.gui.ConfigScreenProvider
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer
import me.shedaniel.clothconfig2.api.AbstractConfigEntry
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.gui.GlobalizedClothConfigScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import java.util.*


object RPMTWConfig {
    private var config: ConfigScreen? = null
    fun register() {
        RPMTWPlatformMod.LOGGER.info("Registering config")
        // register config
        AutoConfig.register(ConfigScreen::class.java) { definition: Config?, configClass: Class<ConfigScreen?>? ->
            JanksonConfigSerializer(definition, configClass)
        }
        config = AutoConfig.getConfigHolder(ConfigScreen::class.java).config
        RPMTWPlatformModPlugin.registerConfigScreen()
        RPMTWPlatformMod.LOGGER.info("Registered config")
    }

    @JvmStatic
    fun get(): ConfigScreen {
        if (config == null) {
            register()
        }
        return config!!
    }

    @Suppress("DEPRECATION", "UnstableApiUsage")
    @JvmStatic
    fun getScreen(parent: Screen?): Screen? {
        if (parent == null) {
            return null
        }

        val provider = AutoConfig.getConfigScreen(ConfigScreen::class.java, parent) as ConfigScreenProvider<*>
        provider.setI13nFunction { "config.rpmtw_platform_mod" }
        provider.setBuildFunction { builder: ConfigBuilder ->
            builder.setGlobalized(true)
            builder.setGlobalizedExpanded(true)
            builder.setAfterInitConsumer { screen ->
                val globalizedScreen: GlobalizedClothConfigScreen = screen as GlobalizedClothConfigScreen

                val entry = LoginButtonEntry()
                entry.setScreen(screen)
                @Suppress("UNCHECKED_CAST") globalizedScreen.listWidget.children()
                    .add(0, entry as AbstractConfigEntry<AbstractConfigEntry<*>>)
            }.build()
        }
        return provider.get()
    }

    fun save() {
        AutoConfig.getConfigHolder(get().javaClass).save()
    }
}

internal class LoginButtonEntry : AbstractConfigListEntry<Any?>(TextComponent(UUID.randomUUID().toString()), false) {

    private var widgets: List<AbstractWidget> = listOf()
    private lateinit var loginButton: Button
    private lateinit var logoutButton: Button

    override fun save() {}

    override fun isMouseInside(mouseX: Int, mouseY: Int, x: Int, y: Int, entryWidth: Int, entryHeight: Int): Boolean {
        return false
    }

    override fun render(
        matrices: PoseStack,
        index: Int,
        y: Int,
        x: Int,
        entryWidth: Int,
        entryHeight: Int,
        mouseX: Int,
        mouseY: Int,
        isHovered: Boolean,
        delta: Float
    ) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta)
        loginButton =
            Button(entryWidth - 65, y, 65, 20, TranslatableComponent("auth.rpmtw_platform_mod.button.login"), {
                RPMTWAuthHandler.login()
            }, { _, matrixStack, i, j ->
                Minecraft.getInstance().screen?.renderTooltip(
                    matrixStack, TranslatableComponent("auth.rpmtw_platform_mod.button.login.tooltip"), i, j
                )
            })

        logoutButton = Button(
            entryWidth + 5, y, 65, 20, TranslatableComponent("auth.rpmtw_platform_mod.button.logout")
        ) {
            RPMTWAuthHandler.logout()
        }

        widgets = listOf<AbstractWidget>(loginButton, logoutButton)
        loginButton.render(matrices, mouseX, mouseY, delta)
        logoutButton.render(matrices, mouseX, mouseY, delta)

        val text =
            I18n.get(if (RPMTWConfig.get().base.rpmtwAuthToken != null) "auth.rpmtw_platform_mod.status.logged_in" else "auth.rpmtw_platform_mod.status.not_logged_in")

        Minecraft.getInstance().font.drawShadow(
            matrices,
            text,
            (x - 4 + entryWidth / 2 - Minecraft.getInstance().font.width(text) / 2).toFloat(),
            (y + 28).toFloat(),
            -1
        )
    }

    override fun children(): List<GuiEventListener?> {
        return widgets
    }

    override fun narratables(): List<NarratableEntry?> {
        return widgets
    }

    override fun getValue(): Any? {
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun getDefaultValue(): Optional<Any?> {
        return Optional.of(Any()) as Optional<Any?>
    }

    override fun getItemHeight(): Int {
        return 35
    }
}
