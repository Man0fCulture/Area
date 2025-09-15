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


class LoginScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        layout = FloatLayout()

        with layout.canvas.before:
            Color(0.2, 0.6, 0.9, 1)
            self.bg_rect = Rectangle(size=layout.size, pos=layout.pos)

        layout.bind(size=self.update_bg, pos=self.update_bg)

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
        self.manager.current = "login"
    
    def check_log(self, instance):
        print("ok")


class RegisterScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        layout = FloatLayout()

        with layout.canvas.before:
            Color(0.2, 0.6, 0.9, 1)
            self.bg_rect = Rectangle(size=layout.size, pos=layout.pos)

        layout.bind(size=self.update_bg, pos=self.update_bg)

        btn = Button(
            text="S'enregistrer",
            size_hint=(None, None),
            size=(200, 100),
            pos=(100, 100)
        )
        btn.bind(on_press=self.go_to_register)
        layout.add_widget(btn)
        self.add_widget(layout)

    def update_bg(self, instance, value):
        self.bg_rect.size = instance.size
        self.bg_rect.pos = instance.pos

    def go_to_register(self, instance):
        self.manager.current = "register"


class MyApp(App):
    def build(self):
        sm = ScreenManager()
        sm.add_widget(LoginScreen(name="register"))
        sm.add_widget(RegisterScreen(name="login"))
        return sm


if __name__ == '__main__':
    MyApp().run()
