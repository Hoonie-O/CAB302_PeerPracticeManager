import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserManagerTest {

    private IUserDAO userdao;
    private UserManager manager;

    @BeforeEach
    public void setUp(){
        userdao = new MockDAO();
        manager = new UserManager(userdao, new BcryptHasher());
    }

    @Test
    public void testSignUpNormal(){

        manager.signUp("Seiji","Sato","sati2030","seigifabian@gmail.com","perra","")



    }

    @Test
    public void testSignUp(){
        manager.signUp("Seiji","Sato","sati2030","seiji@email.com","hola","UQ");

    }

}
