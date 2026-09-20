package io.dynamicconfig.core.watch;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChangeDetectorTest {

    @Test
    void shouldDetectAddedModifiedAndRemovedKeys() {
        ChangeDetector detector = new ChangeDetector();
        List<String> events = new ArrayList<>();

        detector.detectChanges(
                Map.of("removed", "gone", "modified", "old"),
                Map.of("added", "new", "modified", "new"),
                (key, oldValue, newValue) -> events.add(key + ":" + oldValue + "->" + newValue));

        assertEquals(3, events.size());
        assertEquals("added:null->new", events.stream().filter(event -> event.startsWith("added")).findFirst().orElseThrow());
        assertEquals("modified:old->new", events.stream().filter(event -> event.startsWith("modified")).findFirst().orElseThrow());
        assertEquals("removed:gone->null", events.stream().filter(event -> event.startsWith("removed")).findFirst().orElseThrow());
    }

}
