import javax.swing.*

fun createLoginFrame(): JFrame {
    val frame = JFrame("POC KOTLIN - Login")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(400, 300)
    frame.layout = null
    
    // utilisateur
    val userLabel = JLabel("Utilisateur :")
    userLabel.setBounds(50, 50, 100, 30)
    frame.add(userLabel)

    val userText = JTextField()
    userText.setBounds(160, 50, 180, 30)
    frame.add(userText)
    // utilisateur

    // mot de passe
    val passLabel = JLabel("Mot de passe :")
    passLabel.setBounds(50, 100, 100, 30)
    frame.add(passLabel)

    val passText = JPasswordField()
    passText.setBounds(160, 100, 180, 30)
    frame.add(passText)
    // mot de passe

    // Bouton login
    val loginButton = JButton("Se connecter")
    loginButton.setBounds(130, 160, 120, 30)
    frame.add(loginButton)

    loginButton.addActionListener {
        val username = userText.text
        val password = String(passText.password)

        if (username == "admin" && password == "admin") {
            JOptionPane.showMessageDialog(frame, "Connexion réussie 🎉")
        } else {
            JOptionPane.showMessageDialog(frame, "Identifiants invalides ❌")
        }
    }

    return frame
}

fun main() {
    val loginFrame = createLoginFrame()
    loginFrame.isVisible = true
}
