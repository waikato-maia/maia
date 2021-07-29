package māia.main

import māia.configure.json.JSONConfigurationWriter
import māia.configure.Configuration
import māia.configure.ConfigurationElement
import māia.configure.ConfigurationItem
import māia.configure.initialise
import māia.configure.subconfiguration
import māia.configure.visitation.visit


class PointConfiguration : Configuration() {
    @ConfigurationElement.WithMetadata("The X co-ordinate of the point")
    var x by ConfigurationItem<Int>()

    @ConfigurationElement.WithMetadata("The Y co-ordinate of the point")
    var y by ConfigurationItem<Int>()
}

class RectangleConfiguration : Configuration() {
    @ConfigurationElement.WithMetadata("The colour of the rectangle")
    var colour by ConfigurationItem(optional = true) { "blue" }

    @ConfigurationElement.WithMetadata("The top-left corner of the rectangle")
    var topLeft by subconfiguration<PointConfiguration> {
        x = 0
        y = 0
    }

    @ConfigurationElement.WithMetadata("The bottom-right corner of the rectangle")
    var bottomRight by subconfiguration<PointConfiguration> {
        x = 1920
        y = 1080
    }
}


fun main() {
    val rectangleConfig: RectangleConfiguration = initialise {
        topLeft.x = 1
        topLeft.y = 2
        bottomRight.x = 3
        bottomRight.y = 4
        colour = "red"
    }

    println(JSONConfigurationWriter().visit(rectangleConfig).toString())
}
