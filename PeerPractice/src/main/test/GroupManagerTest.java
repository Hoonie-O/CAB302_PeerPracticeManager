import com.cab302.peerpractice.Exceptions.DuplicateGroupException;
import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GroupManagerTest {

    private GroupManager groupManager;
    private IGroupDAO groupDAO;
    private Notifier notifier;
    private IUserDAO userDAO;
    private User user;
    private String NAME = "Group1";
    private String DESCRIPTION = "This is the decription of a group";
    private String USERNAME = "sati2030";
    private Group group;

    @BeforeEach
    public void setUp(){
        groupDAO = new MockGroupDAO();
        userDAO = new MockUserDAO();
        notifier = new Notifier(userDAO);
        groupManager = new GroupManager(groupDAO,notifier,userDAO);
        user = new User("Seiji","Sato",USERNAME,"email@email.com","masfsa","qut");
        group = new Group(NAME,DESCRIPTION,false,user.getUsername(),LocalDateTime.now());
    }

    @Test
    void testCreateGroupNormal(){
        assertDoesNotThrow(() -> {groupManager.createGroup(NAME,DESCRIPTION,false,user);});
    }

    @Test
   void testCreateGroupNullCreator(){
        assertThrows(IllegalArgumentException.class, () -> groupManager.createGroup(NAME,DESCRIPTION,false,null));
    }

    @Test
    void testCreateGroupNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup(null, DESCRIPTION, true, user));
    }

    @Test
    void testCreateGroupBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup("   ", DESCRIPTION, false, user));
    }

    @Test
    void testCreateGroupNameTooLong() {
        String tooLong = "ABCDEFGHIJKLMNOPQRSTU"; // 21 chars
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup(tooLong, DESCRIPTION, false, user));
    }

    @Test
    void testCreateGroupInvalidChars() {
        String invalid = "Bad@Name!";
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup(invalid, DESCRIPTION, true, user));
    }

    @Test
    void testCreateGroupExactly20Chars() {
        String twenty = "ABCDEFGHIJKLMNOPQRST"; // 20 chars
        assertDoesNotThrow(() ->
                groupManager.createGroup(twenty, DESCRIPTION, false, user));
    }

    @Test
    void testCreateGroupValidChars() {
        String valid = "Team_Name-1.2 test";
        assertDoesNotThrow(() ->
                groupManager.createGroup(valid, DESCRIPTION, true, user));
    }

    @Test
    void testCreateGroupNullDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup(NAME, null, true, user));
    }

    @Test
    void testCreateGroupBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup(NAME, "   ", false, user));
    }

    @Test
    void testCreateGroupDescriptionTooLong() {
        String tooLong = "a".repeat(201); // 201 chars
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup(NAME, tooLong, false, user));
    }

    @Test
    void testCreateGroupDescriptionExactly200Chars() {
        String twoHundred = "a".repeat(200);
        assertDoesNotThrow(() ->
                groupManager.createGroup(NAME, twoHundred, true, user));
    }

    @Test
    void testCreateGroupValidDescription() {
        assertDoesNotThrow(() ->
                groupManager.createGroup(NAME, DESCRIPTION, true, user));
    }

    @Test
    void testCreateGroupDuplicateGroup(){
        Group group = new Group(NAME,DESCRIPTION,false,USERNAME, LocalDateTime.now());
        groupDAO.addGroup(group);
        assertThrows(DuplicateGroupException.class, () -> groupManager.createGroup(NAME,DESCRIPTION,false,user));
    }

    @Test
    void testCreateGroupRefelectedInDAO(){
        try{
            groupManager.createGroup(NAME,DESCRIPTION,false,user);
            Group group = new Group(NAME,DESCRIPTION,false,user.getUsername(),LocalDateTime.now());
            List<Group> groups = groupDAO.getAllGroups();
            assertEquals(group, groups.get(0));
        }catch(Exception e){
            fail();
        }
    }

    @Test
    void testCreateGroupContainsCreator(){
        try{
            groupManager.createGroup(NAME,DESCRIPTION,false,user);
            List<Group> groups = groupDAO.getAllGroups();
            Group g = groups.get(0);
            assertTrue(g.getMembers().contains(user));
        }catch(Exception e){
            fail();
        }
    }


    @Test
    void testRequireApprovalNormal(){
        try{
            groupManager.createGroup(NAME,DESCRIPTION,false,user);
            groupManager.requireApproval(group,true);
            assertTrue(group.isRequire_approval());
        }catch(Exception e){
            fail();
        }
    }

    @Test
    void testAddMemberNormal(){

    }




}
