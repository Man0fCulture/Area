#!/usr/bin/env python3
import gradio as gr
import re

# Store for demo purposes (in production, use a database)
users_db = {}

def login(email, password):
    """Handle login logic"""
    # Hardcoded credentials for demo
    if email == "ben@gmail.com" and password == "bentest":
        return "✅ You're successfully logged in! Welcome back!", "", ""
    else:
        return "❌ Email or password invalid", email, password

def signup(fullname, email, password, confirm_password, terms):
    """Handle signup logic with validation"""
    # Email validation
    email_regex = r'^[^\s@]+@[^\s@]+\.[^\s@]+$'
    if not re.match(email_regex, email):
        return "❌ Please enter a valid email address", fullname, email, password, confirm_password, terms

    # Password special character check
    if not re.search(r'[!@#$%^&*()_+\-=\[\]{};\':"\\|,.<>\/?]', password):
        return "❌ Password must include at least one special character", fullname, email, password, confirm_password, terms

    # Password match check
    if password != confirm_password:
        return "❌ Passwords do not match", fullname, email, password, confirm_password, terms

    # Terms acceptance check
    if not terms:
        return "❌ Please agree to the Terms & Conditions", fullname, email, password, confirm_password, terms

    # Success - store user (in production, hash the password)
    users_db[email] = {"fullname": fullname, "password": password}
    return f"✅ Account created successfully for {fullname}! You can now login.", "", "", "", "", False

def create_app():
    """Create the Gradio interface"""

    # Custom CSS for purple theme
    custom_css = """
    .gradio-container {
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }
    .tab-nav button {
        font-size: 18px !important;
        padding: 12px 24px !important;
    }
    .tab-nav button.selected {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
        color: white !important;
    }
    input[type="text"], input[type="email"], input[type="password"] {
        font-size: 16px !important;
        padding: 12px !important;
    }
    .gr-button {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
        color: white !important;
        font-size: 16px !important;
        font-weight: 600 !important;
        padding: 14px !important;
        border: none !important;
    }
    .gr-button:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
    }
    """

    with gr.Blocks(title="Authentication System", css=custom_css, theme=gr.themes.Soft()) as app:
        gr.Markdown(
            """
            # 🔐 Authentication System
            ### Pure Python Implementation with Gradio
            """
        )

        with gr.Tabs():
            # Login Tab
            with gr.TabItem("Login"):
                with gr.Column():
                    login_email = gr.Textbox(
                        label="Email",
                        placeholder="Enter your email",
                        type="email"
                    )
                    login_password = gr.Textbox(
                        label="Password",
                        placeholder="Enter your password",
                        type="password"
                    )
                    remember_me = gr.Checkbox(label="Remember me", value=False)
                    login_btn = gr.Button("Login", variant="primary")
                    login_output = gr.Textbox(label="Status", interactive=False)

                    login_btn.click(
                        fn=login,
                        inputs=[login_email, login_password],
                        outputs=[login_output, login_email, login_password]
                    )

            # Sign Up Tab
            with gr.TabItem("Sign Up"):
                with gr.Column():
                    signup_fullname = gr.Textbox(
                        label="Full Name",
                        placeholder="Enter your full name"
                    )
                    signup_email = gr.Textbox(
                        label="Email",
                        placeholder="Enter your email",
                        type="email"
                    )
                    signup_password = gr.Textbox(
                        label="Password",
                        placeholder="Create a password (must include a special character)",
                        type="password"
                    )
                    signup_confirm = gr.Textbox(
                        label="Confirm Password",
                        placeholder="Confirm your password",
                        type="password"
                    )
                    terms_checkbox = gr.Checkbox(
                        label="I agree to the Terms & Conditions",
                        value=False
                    )
                    signup_btn = gr.Button("Create Account", variant="primary")
                    signup_output = gr.Textbox(label="Status", interactive=False)

                    signup_btn.click(
                        fn=signup,
                        inputs=[signup_fullname, signup_email, signup_password, signup_confirm, terms_checkbox],
                        outputs=[signup_output, signup_fullname, signup_email, signup_password, signup_confirm, terms_checkbox]
                    )

        gr.Markdown(
            """
            ---
            ### Demo Credentials
            - **Email:** ben@gmail.com
            - **Password:** bentest

            ### Features
            - Email format validation
            - Password special character requirement
            - Password confirmation matching
            - Terms & Conditions acceptance
            - Pure Python - no HTML/CSS files
            """
        )

    return app

if __name__ == "__main__":
    app = create_app()
    print("Starting Gradio Authentication App...")
    print("The app will open in your browser automatically.")
    app.launch(server_name="0.0.0.0", server_port=7860, share=False)