import com.cab302.peerpractice.Model.IUserDAO;
import com.cab302.peerpractice.Model.MockDAO;
import com.cab302.peerpractice.Model.UserManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserManagerTest {

    private IUserDAO userdao;
    private UserManager manager;

    @BeforeEach
    public void setUp(){
        userdao = new MockDAO();
        manager = new UserManager(userdao);
    }

    @Test
    public void testGetAllContacts(){

    }

    @Test
    public void testSignUp(){
        manager.signUp("Seiji","Sato","sati2030","seiji@email.com","hola","UQ");

    }

}
