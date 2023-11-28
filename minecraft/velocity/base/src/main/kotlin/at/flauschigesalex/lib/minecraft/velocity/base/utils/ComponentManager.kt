package at.flauschigesalex.lib.minecraft.velocity.base.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

@Suppress("unused")
object ComponentManager {
    @JvmStatic fun spliterator(component: Component): ArrayList<Component> {
        return spliterator(component, ArrayList())
    }

    private fun <E: Any> ArrayList<E>.setLast(value: E): Pair<ArrayList<E>, E?> {
        if (this.isEmpty()) {
            this.add(value)
            return Pair(this, null)
        }

        val old = this.lastOrNull()
        this[this.size -1] = value

        return Pair(this, old)
    }

    private fun spliterator(component: Component, list: ArrayList<Component>): ArrayList<Component> {
        val base = list.lastOrNull() ?: Component.empty()
        val noChildren = component.children(listOf())

        if (noChildren is TextComponent && noChildren.content() == "\n") {
            list.setLast(base)
            list.add(Component.empty())
        } else {
            list.setLast(base.append(noChildren))
        }

        component.children().forEach {
            spliterator(it, list)
        }

        return list
    }

    @JvmStatic fun compromiser(list: List<Component?>?): Component {
        if (list.isNullOrEmpty())
            return Component.empty()

        var base: Component = list.first()!!
        for (i in 1 until list.size) {
            base = base.append(list[i]!!)
        }

        return base
    }
}
