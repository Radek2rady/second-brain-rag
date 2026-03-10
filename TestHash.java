import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$X8OOM8m.E8aL.qFByfONj.Q7V7e.L8c6p.O.K.64L8b.2qE/Y1J7u";
        boolean matches = encoder.matches("password", hash);
        System.out.println("Matches 'password'? " + matches);
    }
}
