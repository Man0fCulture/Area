from kivy.config import Config
Config.set('graphics', 'width', '400')
Config.set('graphics', 'height', '650')
Config.set('graphics', 'resizable', False)

from kivy.app import App
from kivy.uix.screenmanager import ScreenManager, Screen
from kivy.uix.button import Button
from kivy.uix.floatlayout import FloatLayout
from kivy.graphics import Color, Rectangle
from kivy.uix.textinput import TextInput
from kivy.uix.label import Label

class LoginScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.user_data = None
        layout = FloatLayout()

        with layout.canvas.before:
            Color(0.2, 0.6, 0.9, 1)
            self.bg_rect = Rectangle(size=layout.size, pos=layout.pos)

        layout.bind(size=self.update_bg, pos=self.update_bg)

        self.user = None

        self.greeting_label = Label(
            text=f"Bonjour !",
            font_size=24,
            size_hint=(None, None),
            size=(200, 50),
            pos=(100, 500)
        )
        layout.add_widget(self.greeting_label)

        self.username = TextInput(
            hint_text="Nom d'utilisateur",
            multiline=False,
            size_hint=(None, None),
            size=(200, 50),
            pos=(100, 380)
        )

        self.password = TextInput(
            hint_text="Mot de passe",
            multiline=False,
            size_hint=(None, None),
            size=(200, 50),
            pos=(100, 320)
        )

        layout.add_widget(self.username)
        layout.add_widget(self.password)

        btn_log = Button(
            text="Connexion",
            size_hint=(None, None),
            size=(200, 100),
            pos=(100, 200)
        )

        btn = Button(
            text="Créer un compte",
            size_hint=(None, None),
            size=(200, 100),
            pos=(100, 75)
        )
        btn.bind(on_press=self.go_to_login)
        btn_log.bind(on_press=self.check_log)
        layout.add_widget(btn)
        layout.add_widget(btn_log)
        self.add_widget(layout)

    def update_bg(self, instance, value):
        self.bg_rect.size = instance.size
        self.bg_rect.pos = instance.pos

    def go_to_login(self, instance):
        self.manager.current = "register"
    
    def check_log(self, instance):
        self.user_data = self.app.user_data
        if self.user_data == None:
            return

        user_text = self.username.text
        password_text = self.password.text
        print(user_text, password_text)
        if user_text == self.user_data["username"] and password_text == self.user_data["password"]:
            print("IN")
            self.user = user_text
            self.greeting_label.text = f"Bonjour, {self.user} !"


class RegisterScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.user_data = None
        layout = FloatLayout()

        with layout.canvas.before:
            Color(0.2, 0.6, 0.9, 1)
            self.bg_rect = Rectangle(size=layout.size, pos=layout.pos)

        layout.bind(size=self.update_bg, pos=self.update_bg)

        self.greeting_label = Label(
            text=f"Créer un compte !",
            font_size=24,
            size_hint=(None, None),
            size=(200, 50),
            pos=(100, 500)
        )
        layout.add_widget(self.greeting_label)

        self.username = TextInput(
            hint_text="Nom d'utilisateur",
            multiline=False,
            size_hint=(None, None),
            size=(200, 50),
            pos=(100, 380)
        )

        self.password = TextInput(
            hint_text="Mot de passe",
            multiline=False,
            size_hint=(None, None),
            size=(200, 50),
            pos=(100, 320)
        )

        layout.add_widget(self.username)
        layout.add_widget(self.password)

        btn_register = Button(
            text="S'enregister !",
            size_hint=(None, None),
            size=(200, 100),
            pos=(100, 210)
        )

        btn = Button(
            text="Déja un compte?",
            size_hint=(None, None),
            size=(200, 100),
            pos=(100, 100)
        )
        btn_register.bind(on_press=self.create_user)
        btn.bind(on_press=self.go_to_register)
        layout.add_widget(btn)
        layout.add_widget(btn_register)
        self.add_widget(layout)

    def update_bg(self, instance, value):
        self.bg_rect.size = instance.size
        self.bg_rect.pos = instance.pos

    def go_to_register(self, instance):
        self.manager.current = "login"
    
    def create_user(self, instance):
        if self.username.text != None and self.password.text != None:
            self.user_data = self.app.user_data
            self.user_data['username'] = self.username.text
            self.user_data['password'] = self.password.text
            print("Utilisateur enregistré :", self.user_data)
            self.go_to_register(instance)


class MyApp(App):
    def build(self):
        self.user_data = {}
        sm = ScreenManager()
        self.login_screen = LoginScreen(name="login")
        self.register_screen = RegisterScreen(name="register")

        self.login_screen.app = self
        self.register_screen.app = self

        sm.add_widget(self.login_screen)
        sm.add_widget(self.register_screen)

        return sm


if __name__ == '__main__':
    MyApp().run()
