package com.akgaming.huntersystem.ai

import org.junit.Assert.*
import org.junit.Test

class ModelCatalogTest {
    @Test fun `catalog uses pinned https source and sha256`() {
        val model = ModelCatalog.models.single()
        assertTrue(model.sourceUrl.startsWith("https://huggingface.co/"))
        assertEquals(64, model.sha256.length)
        assertTrue(model.sha256.all { it in '0'..'9' || it in 'a'..'f' })
        assertTrue(model.bytes > 100_000_000)
    }
    @Test fun `runtime honestly reports unsupported`() { assertFalse(RuntimeCapabilityProbe().capability(ModelCatalog.models.single()).supported) }
}
