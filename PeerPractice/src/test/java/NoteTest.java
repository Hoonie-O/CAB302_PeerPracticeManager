import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class NoteTest {

    @Test
    public void TestCreateNoteNormal(){
        assertDoesNotThrow(() -> new Note("name",1));
    }


    @Test
    public void TestGetName(){
        Note note = new Note("name",1);
        assertEquals("name",note.getName());
    }

    @Test
    public void TestChangeName(){
        Note note = new Note("name",1);
        note.setName("other");
        assertEquals("other",note.getName());
    }

    @Test
    public void TestGetGroup(){
        Note note = new Note("name",1);
        assertEquals(1,note.getGroup());
    }

    public void TestGetIDNullUponCreation(){
        Note note = new Note("name",1);
        assertNull(note.getID());
    }


    @Test
    public void TestSetID(){
        Note note = new Note("name",1);
        String ID = UUID.randomUUID().toString();
        note.setID(ID);
        assertEquals(ID,note.getID());
    }

    @Test
    public void TestNullChaptersUponCreation(){
        Note note = new Note("name",1);
        assertNull(note.getChapters());
    }

    @Test
    public void TestAddChapter(){
        Note note = new Note("name",1);
        Chapter chapter = new Chapter("name","1");
        note.addChapter("1");
        assertEquals(1, note.getChapters());
        assertSame(chapter, note.getChapters().getFirst());
    }

    @Test
    public void TestChaptersUnmodifiableList(){
        Note note = new Note("name",1);
        Chapter chapter = new Chapter("name","1");
        note.addChapter("1");
        assertThrows(UnsupportedOperationException.class, () -> note.getChapters().add("s"));
    }






}
