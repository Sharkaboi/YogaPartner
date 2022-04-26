package com.sharkaboi.yogapartner.ml.models

/** Describing a frame info.  */
class BitmapFrameMetadata
private constructor(
    val width: Int,
    val height: Int,
    val rotation: Int
) {
    /** Builder of [BitmapFrameMetadata].  */
    class Builder {
        private var width = 0
        private var height = 0
        private var rotation = 0

        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }

        fun setRotation(rotation: Int): Builder {
            this.rotation = rotation
            return this
        }

        fun build(): BitmapFrameMetadata {
            return BitmapFrameMetadata(width, height, rotation)
        }
    }
}