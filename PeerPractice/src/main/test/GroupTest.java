import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GroupTest {

    private Group group;
    private User user;
    private String USERNAME = "sati2030";
    private LocalDateTime localDateTime;

    @BeforeEach
    public void setUp(){
        user = new User("Seiji","Sato",USERNAME,"email@email.com","asfasfasfasf","QUT");
        localDateTime = LocalDateTime.now();
        group = new Group("Group1","Group for CAB302",false,USERNAME, localDateTime);
    }

    @Test
    public void testGetSetMembers(){
        List<User> users = new ArrayList<>();
        users.add(user);
        group.setMembers(users);
        assertEquals(users,group.getMembers());
    }

    @Test
    public void testAddMembers(){
        User user2 = new User("Cristiano","Ronaldo","cristiano7","cr7@email.com","asfasf","qut");
        group.addMember(user2);
        assertTrue(group.getMembers().contains(user2));
    }

    @Test
    public void testGetName(){
        assertEquals("Group1",group.getName());
    }

    @Test
    public void testSetName(){
        group.setName("Group2");
        assertEquals("Group2",group.getName());
    }

    @Test
    public void testSetID(){
        assertDoesNotThrow(() -> {group.setID(123);});
    }

    @Test
    public void testGetID(){
        group.setID(123);
        assertEquals(123,group.getID());
    }

    @Test
    public void testGetDescription(){
        assertEquals("Group for CAB302",group.getDescription());
    }

    @Test
    public void testSetDescription(){
        group.setDescription("Hola");
        assertEquals("Hola",group.getDescription());
    }

    @Test
    public void testGetOwner(){
        assertEquals(USERNAME,group.getOwner());
    }

    @Test
    public void testSetOwner(){
        group.setOwner("someone");
        assertEquals("someone",group.getOwner());
    }

    @Test
    public void testIsRequire_Approval(){
        assertFalse(group.isRequire_approval());
    }

    @Test
    public void testGetRequire_Approval(){
        group.setRequire_approval(true);
        assertTrue(group.isRequire_approval());
    }

    @Test
    public void testGetCreated_at(){
        assertEquals(localDateTime,group.getCreated_at());
    }


    @Test
    public void testGroupConstructorAllParameters(){
        Group newGroup = new Group("TestGroup", "Description", true, "owner123", localDateTime);
        assertEquals("TestGroup", newGroup.getName());
        assertEquals("Description", newGroup.getDescription());
        assertTrue(newGroup.isRequire_approval());
        assertEquals("owner123", newGroup.getOwner());
        assertEquals(localDateTime, newGroup.getCreated_at());
    }



    @Test
    public void testSetNameLong(){
        String longName = "a".repeat(100);
        assertDoesNotThrow(() -> group.setName(longName));
        assertEquals(longName, group.getName());
    }

    @Test
    public void testSetNameSpecialCharacters(){
        String specialName = "Group-1_Test@2024!";
        assertDoesNotThrow(() -> group.setName(specialName));
        assertEquals(specialName, group.getName());
    }

    @Test
    public void testSetDescriptionNull(){
        assertDoesNotThrow(() -> group.setDescription(null));
        assertNull(group.getDescription());
    }

    @Test
    public void testSetDescriptionEmpty(){
        assertDoesNotThrow(() -> group.setDescription(""));
        assertEquals("", group.getDescription());
    }

    @Test
    public void testSetDescriptionLong(){
        String longDescription = "This is a very long description that contains multiple sentences and should be handled properly by the group class. ".repeat(10);
        assertDoesNotThrow(() -> group.setDescription(longDescription));
        assertEquals(longDescription, group.getDescription());
    }



    @Test
    public void testAddMemberSameUserMultipleTimes(){
        group.addMember(user);
        group.addMember(user);
        assertEquals(2, group.getMembers().size());
    }


    @Test
    public void testGetMembersInitiallyEmpty(){
        assertNotNull(group.getMembers());
        assertEquals(0, group.getMembers().size());
    }


    @Test
    public void testSetMembersEmpty(){
        List<User> emptyList = new ArrayList<>();
        assertDoesNotThrow(() -> group.setMembers(emptyList));
        assertEquals(0, group.getMembers().size());
    }

    @Test
    public void testSetMembersLargeList(){
        List<User> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeList.add(new User("User" + i, "Last" + i, "user" + i, "user" + i + "@email.com", "pass", "Uni"));
        }
        assertDoesNotThrow(() -> group.setMembers(largeList));
        assertEquals(100, group.getMembers().size());
    }



    @Test
    public void testGroupIdNotNull(){
        assertNotNull(group.getID());
    }




    @Test
    public void testCreatedAtNotNull(){
        assertNotNull(group.getCreated_at());
    }



}
