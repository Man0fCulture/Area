#!/usr/bin/env python3
import tkinter as tk
from tkinter import ttk, messagebox
from tkinter import font as tkfont
import re
from PIL import Image, ImageDraw, ImageTk
import io

class AuthenticationApp:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Authentication System")
        self.root.geometry("450x600")
        self.root.resizable(False, False)

        # Colors matching the CSS gradient
        self.purple1 = "#667eea"
        self.purple2 = "#764ba2"
        self.white = "#ffffff"
        self.gray = "#666666"
        self.light_gray = "#dddddd"
        self.error_bg = "#ffeeee"
        self.error_text = "#cc3333"

        # Fonts
        self.title_font = tkfont.Font(family="Helvetica", size=24, weight="bold")
        self.label_font = tkfont.Font(family="Helvetica", size=11)
        self.input_font = tkfont.Font(family="Helvetica", size=12)
        self.button_font = tkfont.Font(family="Helvetica", size=14, weight="bold")
        self.link_font = tkfont.Font(family="Helvetica", size=10, underline=True)

        # Create gradient background
        self.create_gradient_background()

        # Start with login screen
        self.show_login()

    def create_gradient_background(self):
        """Create a gradient background similar to the CSS"""
        width = 450
        height = 600

        # Create gradient image
        gradient = Image.new('RGB', (width, height))
        draw = ImageDraw.Draw(gradient)

        # Create gradient from purple1 to purple2
        for i in range(height):
            # Interpolate colors
            ratio = i / height
            r1, g1, b1 = int(self.purple1[1:3], 16), int(self.purple1[3:5], 16), int(self.purple1[5:7], 16)
            r2, g2, b2 = int(self.purple2[1:3], 16), int(self.purple2[3:5], 16), int(self.purple2[5:7], 16)

            r = int(r1 * (1 - ratio) + r2 * ratio)
            g = int(g1 * (1 - ratio) + g2 * ratio)
            b = int(b1 * (1 - ratio) + b2 * ratio)

            draw.rectangle([(0, i), (width, i + 1)], fill=(r, g, b))

        self.gradient_photo = ImageTk.PhotoImage(gradient)

    def clear_window(self):
        """Clear all widgets from the window"""
        for widget in self.root.winfo_children():
            widget.destroy()

    def create_form_container(self):
        """Create the white rounded container for forms"""
        # Background with gradient
        bg_label = tk.Label(self.root, image=self.gradient_photo)
        bg_label.place(x=0, y=0, relwidth=1, relheight=1)

        # White container frame
        container = tk.Frame(self.root, bg=self.white, relief=tk.RAISED, borderwidth=2)
        container.place(relx=0.5, rely=0.5, anchor="center", width=380, height=500)

        return container

    def show_login(self):
        """Display the login screen"""
        self.clear_window()
        container = self.create_form_container()

        # Title
        title = tk.Label(container, text="Login", font=self.title_font, bg=self.white, fg="#333333")
        title.pack(pady=(30, 20))

        # Error message label (initially hidden)
        self.error_label = tk.Label(container, text="", font=self.label_font,
                                   bg=self.error_bg, fg=self.error_text,
                                   wraplength=320, pady=10)

        # Email field
        email_label = tk.Label(container, text="Email", font=self.label_font,
                              bg=self.white, fg=self.gray, anchor="w")
        email_label.pack(fill="x", padx=30, pady=(20, 5))

        self.email_entry = tk.Entry(container, font=self.input_font, relief=tk.SOLID,
                                   borderwidth=1, highlightthickness=2,
                                   highlightcolor=self.purple1)
        self.email_entry.pack(fill="x", padx=30, pady=(0, 10), ipady=8)
        self.email_entry.insert(0, "Enter your email")
        self.email_entry.bind("<FocusIn>", lambda e: self.clear_placeholder(e, "Enter your email"))
        self.email_entry.bind("<FocusOut>", lambda e: self.restore_placeholder(e, "Enter your email"))

        # Password field
        password_label = tk.Label(container, text="Password", font=self.label_font,
                                bg=self.white, fg=self.gray, anchor="w")
        password_label.pack(fill="x", padx=30, pady=(10, 5))

        self.password_entry = tk.Entry(container, font=self.input_font, show="*",
                                      relief=tk.SOLID, borderwidth=1,
                                      highlightthickness=2, highlightcolor=self.purple1)
        self.password_entry.pack(fill="x", padx=30, pady=(0, 10), ipady=8)
        self.password_entry.insert(0, "password")
        self.password_entry.bind("<FocusIn>", lambda e: self.clear_password_placeholder(e))
        self.password_entry.bind("<FocusOut>", lambda e: self.restore_password_placeholder(e))

        # Remember me and forgot password frame
        options_frame = tk.Frame(container, bg=self.white)
        options_frame.pack(fill="x", padx=30, pady=(10, 20))

        self.remember_var = tk.BooleanVar()
        remember_check = tk.Checkbutton(options_frame, text="Remember me",
                                       variable=self.remember_var, font=self.label_font,
                                       bg=self.white, fg=self.gray, activebackground=self.white)
        remember_check.pack(side="left")

        forgot_link = tk.Label(options_frame, text="Forgot password?",
                             font=self.link_font, bg=self.white, fg=self.purple1,
                             cursor="hand2")
        forgot_link.pack(side="right")
        forgot_link.bind("<Button-1>", lambda e: messagebox.showinfo("Info", "Password reset not implemented"))

        # Login button
        login_btn = tk.Button(container, text="Login", font=self.button_font,
                            bg=self.purple1, fg=self.white, relief=tk.FLAT,
                            cursor="hand2", command=self.handle_login,
                            activebackground=self.purple2)
        login_btn.pack(fill="x", padx=30, pady=(10, 20), ipady=10)

        # Sign up link
        signup_frame = tk.Frame(container, bg=self.white)
        signup_frame.pack(pady=(10, 0))

        signup_text = tk.Label(signup_frame, text="Don't have an account? ",
                             font=self.label_font, bg=self.white, fg=self.gray)
        signup_text.pack(side="left")

        signup_link = tk.Label(signup_frame, text="Sign up", font=self.link_font,
                             bg=self.white, fg=self.purple1, cursor="hand2")
        signup_link.pack(side="left")
        signup_link.bind("<Button-1>", lambda e: self.show_signup())

    def show_signup(self):
        """Display the signup screen"""
        self.clear_window()
        container = self.create_form_container()

        # Title
        title = tk.Label(container, text="Sign Up", font=self.title_font,
                        bg=self.white, fg="#333333")
        title.pack(pady=(30, 20))

        # Error message label (initially hidden)
        self.error_label = tk.Label(container, text="", font=self.label_font,
                                   bg=self.error_bg, fg=self.error_text,
                                   wraplength=320, pady=10)

        # Create a frame with scrollbar for form fields
        form_frame = tk.Frame(container, bg=self.white)
        form_frame.pack(fill="both", expand=True, padx=30)

        # Full Name field
        name_label = tk.Label(form_frame, text="Full Name", font=self.label_font,
                            bg=self.white, fg=self.gray, anchor="w")
        name_label.pack(fill="x", pady=(10, 5))

        self.name_entry = tk.Entry(form_frame, font=self.input_font, relief=tk.SOLID,
                                  borderwidth=1, highlightthickness=2,
                                  highlightcolor=self.purple1)
        self.name_entry.pack(fill="x", pady=(0, 10), ipady=6)

        # Email field
        email_label = tk.Label(form_frame, text="Email", font=self.label_font,
                             bg=self.white, fg=self.gray, anchor="w")
        email_label.pack(fill="x", pady=(5, 5))

        self.signup_email_entry = tk.Entry(form_frame, font=self.input_font, relief=tk.SOLID,
                                          borderwidth=1, highlightthickness=2,
                                          highlightcolor=self.purple1)
        self.signup_email_entry.pack(fill="x", pady=(0, 10), ipady=6)

        # Password field with hint
        password_frame = tk.Frame(form_frame, bg=self.white)
        password_frame.pack(fill="x", pady=(5, 5))

        password_label = tk.Label(password_frame, text="Password", font=self.label_font,
                                bg=self.white, fg=self.gray)
        password_label.pack(side="left")

        password_hint = tk.Label(password_frame, text="(must include a special character)",
                               font=("Helvetica", 9), bg=self.white, fg="#999999")
        password_hint.pack(side="left", padx=(5, 0))

        self.signup_password_entry = tk.Entry(form_frame, font=self.input_font, show="*",
                                             relief=tk.SOLID, borderwidth=1,
                                             highlightthickness=2, highlightcolor=self.purple1)
        self.signup_password_entry.pack(fill="x", pady=(0, 10), ipady=6)

        # Confirm Password field
        confirm_label = tk.Label(form_frame, text="Confirm Password", font=self.label_font,
                                bg=self.white, fg=self.gray, anchor="w")
        confirm_label.pack(fill="x", pady=(5, 5))

        self.confirm_password_entry = tk.Entry(form_frame, font=self.input_font, show="*",
                                              relief=tk.SOLID, borderwidth=1,
                                              highlightthickness=2, highlightcolor=self.purple1)
        self.confirm_password_entry.pack(fill="x", pady=(0, 10), ipady=6)

        # Terms checkbox
        self.terms_var = tk.BooleanVar()
        terms_check = tk.Checkbutton(form_frame, text="I agree to the Terms & Conditions",
                                    variable=self.terms_var, font=self.label_font,
                                    bg=self.white, fg=self.gray, activebackground=self.white)
        terms_check.pack(pady=(10, 15))

        # Create Account button
        signup_btn = tk.Button(form_frame, text="Create Account", font=self.button_font,
                             bg=self.purple1, fg=self.white, relief=tk.FLAT,
                             cursor="hand2", command=self.handle_signup,
                             activebackground=self.purple2)
        signup_btn.pack(fill="x", pady=(10, 15), ipady=10)

        # Login link
        login_frame = tk.Frame(form_frame, bg=self.white)
        login_frame.pack(pady=(5, 10))

        login_text = tk.Label(login_frame, text="Already have an account? ",
                            font=self.label_font, bg=self.white, fg=self.gray)
        login_text.pack(side="left")

        login_link = tk.Label(login_frame, text="Login", font=self.link_font,
                            bg=self.white, fg=self.purple1, cursor="hand2")
        login_link.pack(side="left")
        login_link.bind("<Button-1>", lambda e: self.show_login())

    def show_login_success(self):
        """Display the login success screen"""
        self.clear_window()
        container = self.create_form_container()

        # Success icon (checkmark)
        icon_frame = tk.Frame(container, bg=self.purple1, width=80, height=80)
        icon_frame.pack(pady=(60, 20))
        icon_frame.pack_propagate(False)

        checkmark = tk.Label(icon_frame, text="✓", font=("Helvetica", 48, "bold"),
                           bg=self.purple1, fg=self.white)
        checkmark.place(relx=0.5, rely=0.5, anchor="center")

        # Success message
        title = tk.Label(container, text="You're successfully logged in!",
                       font=self.title_font, bg=self.white, fg="#333333")
        title.pack(pady=(20, 10))

        message = tk.Label(container, text="Welcome back! You have been successfully authenticated.",
                         font=self.label_font, bg=self.white, fg=self.gray,
                         wraplength=320)
        message.pack(pady=(10, 40))

        # Logout button
        logout_btn = tk.Button(container, text="Logout", font=self.button_font,
                             bg=self.purple1, fg=self.white, relief=tk.FLAT,
                             cursor="hand2", command=self.show_login,
                             activebackground=self.purple2, padx=40)
        logout_btn.pack(pady=(10, 20), ipady=10)

    def show_signup_success(self):
        """Display the signup success screen"""
        self.clear_window()
        container = self.create_form_container()

        # Success icon (checkmark)
        icon_frame = tk.Frame(container, bg=self.purple1, width=80, height=80)
        icon_frame.pack(pady=(60, 20))
        icon_frame.pack_propagate(False)

        checkmark = tk.Label(icon_frame, text="✓", font=("Helvetica", 48, "bold"),
                           bg=self.purple1, fg=self.white)
        checkmark.place(relx=0.5, rely=0.5, anchor="center")

        # Success message
        title = tk.Label(container, text="You have successfully\ncreated your account!",
                       font=self.title_font, bg=self.white, fg="#333333")
        title.pack(pady=(20, 10))

        message = tk.Label(container, text="Your account has been created. You can now login with your credentials.",
                         font=self.label_font, bg=self.white, fg=self.gray,
                         wraplength=320)
        message.pack(pady=(10, 40))

        # Go to Login button
        login_btn = tk.Button(container, text="Go to Login", font=self.button_font,
                            bg=self.purple1, fg=self.white, relief=tk.FLAT,
                            cursor="hand2", command=self.show_login,
                            activebackground=self.purple2, padx=30)
        login_btn.pack(pady=(10, 20), ipady=10)

    def handle_login(self):
        """Handle login form submission"""
        email = self.email_entry.get()
        password = self.password_entry.get()

        # Clear placeholder text check
        if email == "Enter your email":
            email = ""
        if password == "password" and not self.password_entry.cget("show"):
            password = ""

        # Hardcoded credentials
        valid_email = "ben@gmail.com"
        valid_password = "bentest"

        if email == valid_email and password == valid_password:
            self.show_login_success()
        else:
            self.show_error("Email or password invalid")

    def handle_signup(self):
        """Handle signup form submission"""
        fullname = self.name_entry.get()
        email = self.signup_email_entry.get()
        password = self.signup_password_entry.get()
        confirm_password = self.confirm_password_entry.get()
        terms = self.terms_var.get()

        # Email validation regex
        email_regex = r'^[^\s@]+@[^\s@]+\.[^\s@]+$'

        # Check if email is correctly formatted
        if not re.match(email_regex, email):
            self.show_error("Please enter a valid email address")
            return

        # Check for special character in password
        special_char_regex = r'[!@#$%^&*()_+\-=\[\]{};\':"\\|,.<>\/?]'
        if not re.search(special_char_regex, password):
            self.show_error("Password must include at least one special character")
            return

        # Check if passwords match
        if password != confirm_password:
            self.show_error("Passwords do not match")
            return

        # Check if terms are accepted
        if not terms:
            self.show_error("Please check the box to agree to the Terms & Conditions")
            return

        # All validation passed
        self.show_signup_success()

    def show_error(self, message):
        """Display error message"""
        self.error_label.config(text=message)
        self.error_label.pack(after=self.root.winfo_children()[1].winfo_children()[0],
                            fill="x", padx=30, pady=(0, 10))

    def clear_placeholder(self, event, placeholder):
        """Clear placeholder text on focus"""
        if event.widget.get() == placeholder:
            event.widget.delete(0, tk.END)
            event.widget.config(fg="black")

    def restore_placeholder(self, event, placeholder):
        """Restore placeholder text if empty"""
        if not event.widget.get():
            event.widget.insert(0, placeholder)
            event.widget.config(fg=self.gray)

    def clear_password_placeholder(self, event):
        """Clear password placeholder on focus"""
        if event.widget.get() == "password":
            event.widget.delete(0, tk.END)

    def restore_password_placeholder(self, event):
        """Restore password placeholder if empty"""
        if not event.widget.get():
            event.widget.insert(0, "password")

    def run(self):
        """Start the application"""
        self.root.mainloop()

if __name__ == "__main__":
    # Check for required packages
    try:
        from PIL import Image, ImageDraw, ImageTk
    except ImportError:
        print("Installing required package: Pillow...")
        import subprocess
        subprocess.check_call(["pip", "install", "Pillow"])
        from PIL import Image, ImageDraw, ImageTk

    app = AuthenticationApp()
    app.run()