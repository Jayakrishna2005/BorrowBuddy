from selenium.webdriver.common.by import By
from automation.pages.base_page import BasePage

class LoginPage(BasePage):
    # Locators
    EMAIL_INPUT = (By.CSS_SELECTOR, "input[type='email']")
    PASSWORD_INPUT = (By.CSS_SELECTOR, "input[type='password']")
    LOGIN_BUTTON = (By.CLASS_NAME, "btn-primary")
    REGISTER_LINK = (By.XPATH, "//span[contains(text(), 'Register')]")
    FORGOT_PASSWORD_LINK = (By.XPATH, "//span[contains(text(), 'Forgot Password')]")

    def __init__(self, driver, base_url=None):
        super().__init__(driver, base_url)

    def login(self, email, password):
        self.enter_text(self.EMAIL_INPUT, email)
        self.enter_text(self.PASSWORD_INPUT, password)
        self.click(self.LOGIN_BUTTON)

    def navigate_to_register(self):
        self.click(self.REGISTER_LINK)

    def navigate_to_forgot_password(self):
        self.click(self.FORGOT_PASSWORD_LINK)
