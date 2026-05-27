package org.example.voterreview.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecordNormalizerTest {
    private final RecordNormalizer normalizer = new RecordNormalizer();

    @Test
    void normalizesIdentifiersAndNames() {
        assertThat(normalizer.normalizeIdentifier(" ABC 1234 ")).isEqualTo("abc1234");
        assertThat(normalizer.normalizeText("Smt. Laxmi Devi")).isEqualTo("smt laxmi devi");
    }

    @Test
    void producesPhoneticMatchesForSimilarNames() {
        assertThat(normalizer.phoneticMatch("Smith", "Smyth")).isTrue();
        assertThat(normalizer.similarity("Smith", "Smyth")).isGreaterThan(0.7);
    }
}
