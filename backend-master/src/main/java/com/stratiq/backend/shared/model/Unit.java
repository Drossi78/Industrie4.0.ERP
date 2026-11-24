package com.stratiq.backend.shared.model;

public enum Unit {
    // Measurement units for items and batches: counts, weights, volumes, distances, areas, time, sets.
    piece,     // Individual units
    kg,        // Kilogram (weight)
    g,         // Gram (weight)
    l,         // Liter (volume)
    ml,        // Milliliter (volume)
    m,         // Meter (length)
    cm,        // Centimeter (length)
    m2,        // Square meter (area)
    m3,        // Cubic meter (volume)
    hour,      // Hour (time)
    day,       // Day (time)
    set        // Set/kit (grouped items)
}
