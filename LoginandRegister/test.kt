import javax.swing.*

fun createLoginFrame(): JFrame {
    val frame = JFrame("POC KOTLIN - Login")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(400, 300)
    frame.layout = null

    val userLabel = JLabel("Utilisateur :")
    userLabel.setBounds(50, 50, 100, 30)
    frame.add(userLabel)

    val userText = JTextField()
    userText.setBounds(160, 50, 180, 30)
    frame.add(userText)

    val passLabel = JLabel("Mot de passe :")
    passLabel.setBounds(50, 100, 100, 30)
    frame.add(passLabel)

    val passText = JPasswordField()
    passText.setBounds(160, 100, 180, 30)
    frame.add(passText)

    val loginButton = JButton("Se connecter")
    loginButton.setBounds(130, 160, 120, 30)
    frame.add(loginButton)

    val registerButton = JButton("Créer un compte")
    registerButton.setBounds(120, 200, 140, 30)
    frame.add(registerButton)

    loginButton.addActionListener {
        val username = userText.text
        val password = String(passText.password)

        if (username == "admin" && password == "admin") {
            JOptionPane.showMessageDialog(frame, "Connexion réussie 🎉")
        } else {
            JOptionPane.showMessageDialog(frame, "Identifiants invalides ❌")
        }
    }

    registerButton.addActionListener {
        frame.dispose()
        val registerFrame = createRegisterFrame()
        registerFrame.isVisible = true
    }

    return frame
}

fun createRegisterFrame(): JFrame {
    val frame = JFrame("POC KOTLIN - Register")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(450, 350)
    frame.layout = null

    val userLabel = JLabel("Utilisateur :")
    userLabel.setBounds(50, 50, 100, 30)
    frame.add(userLabel)

    val userText = JTextField()
    userText.setBounds(170, 50, 200, 30)
    frame.add(userText)

    val passLabel = JLabel("Mot de passe :")
    passLabel.setBounds(50, 100, 100, 30)
    frame.add(passLabel)

    val passText = JPasswordField()
    passText.setBounds(170, 100, 200, 30)
    frame.add(passText)

    val confirmLabel = JLabel("Confirmer :")
    confirmLabel.setBounds(50, 150, 100, 30)
    frame.add(confirmLabel)

    val confirmText = JPasswordField()
    confirmText.setBounds(170, 150, 200, 30)
    frame.add(confirmText)

    val registerButton = JButton("S'inscrire")
    registerButton.setBounds(150, 210, 120, 30)
    frame.add(registerButton)

    val backButton = JButton("Retour")
    backButton.setBounds(150, 250, 120, 30)
    frame.add(backButton)

    registerButton.addActionListener {
        val username = userText.text
        val password = String(passText.password)
        val confirm = String(confirmText.password)

        if (username.isBlank() || password.isBlank() || confirm.isBlank()) {
            JOptionPane.showMessageDialog(frame, "Champs incomplets ❌")
        } else if (password != confirm) {
            JOptionPane.showMessageDialog(frame, "Les mots de passe ne correspondent pas ❌")
        } else {
            JOptionPane.showMessageDialog(frame, "Inscription réussie 🎉")
        }
    }

    backButton.addActionListener {
        frame.dispose()
        val loginFrame = createLoginFrame()
        loginFrame.isVisible = true
    }

    return frame
}

fun main() {
    val loginFrame = createLoginFrame()
    loginFrame.isVisible = true
}
