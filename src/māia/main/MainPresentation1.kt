/*
 * MainPresentation1.kt
 * Copyright (C) 2021 University of Waikato, Hamilton, New Zealand
 *
 * This file is part of MĀIA.
 *
 * MĀIA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MĀIA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MĀIA.  If not, see <https://www.gnu.org/licenses/>.
 */
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
