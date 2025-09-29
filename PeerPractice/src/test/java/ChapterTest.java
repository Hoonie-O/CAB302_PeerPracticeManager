import com.cab302.peerpractice.Model.Entities.*;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChapterTest {

    @Test
    void constructorInitialState() {
        Chapter ch = new Chapter("Intro", "note-123");

        assertNull(ch.getID());
        assertEquals("Intro", ch.getName());
        assertEquals("note-123", ch.getNote());
        assertEquals("", ch.getContent());

        List<Attachment> atts = ch.getAttachments();
        assertNotNull(atts);
        assertTrue(atts.isEmpty());
    }

    @Test
    void settersUpdateFields() {
        Chapter ch = new Chapter("Intro", "note-123");

        ch.setID("chap-001");
        ch.setName("Chapter 1");
        ch.setContent("Once upon a time...");

        assertEquals("chap-001", ch.getID());
        assertEquals("Chapter 1", ch.getName());
        assertEquals("Once upon a time...", ch.getContent());
    }

    @Test
    void addAttachmentAdds() {
        Chapter ch = new Chapter("Intro", "note-123");
        Attachment att = new Attachment("file.txt", "/tmp/file.txt", 100L, "text/plain");

        ch.addAttachment(att);

        List<Attachment> atts = ch.getAttachments();
        assertEquals(1, atts.size());
        assertSame(att, atts.get(0));
    }

    @Test
    void attachmentsAreUnmodifiable() {
        Chapter ch = new Chapter("Intro", "note-123");
        Attachment att = new Attachment("file.txt", "/tmp/file.txt", 100L, "text/plain");
        ch.addAttachment(att);

        List<Attachment> attsView = ch.getAttachments();
        assertThrows(UnsupportedOperationException.class, () -> attsView.add(
                new Attachment("file2.txt", "/tmp/file2.txt", 200L, "text/plain")));
    }

    @Test
    void noteIdIsImmutable() {
        Chapter ch = new Chapter("Intro", "note-xyz");
        assertEquals("note-xyz", ch.getNote());
    }
}
