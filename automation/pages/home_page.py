from selenium.webdriver.common.by import By
from automation.pages.base_page import BasePage

class HomePage(BasePage):
    # Locators
    SEARCH_INPUT = (By.CSS_SELECTOR, "input[placeholder*='Search']")
    NAV_LINK_REQUESTS = (By.LINK_TEXT, "Requests")
    NAV_LINK_POST = (By.LINK_TEXT, "Post Item")
    NAV_LINK_LEADERBOARD = (By.LINK_TEXT, "Leaderboard")
    NAV_LINK_PROFILE = (By.LINK_TEXT, "Profile")
    LOGOUT_BTN = (By.XPATH, "//div[contains(text(), 'Logout')]")
    ITEM_CARDS = (By.CLASS_NAME, "glass-card")
    POST_SUCCESS_TOAST = (By.CLASS_NAME, "toast") # If toast exists

    def __init__(self, driver, base_url=None):
        super().__init__(driver, base_url)

    def search_items(self, query):
        self.enter_text(self.SEARCH_INPUT, query)

    def navigate_to_requests(self):
        self.click(self.NAV_LINK_REQUESTS)

    def navigate_to_post_item(self):
        self.click(self.NAV_LINK_POST)

    def navigate_to_leaderboard(self):
        self.click(self.NAV_LINK_LEADERBOARD)

    def navigate_to_profile(self):
        self.click(self.NAV_LINK_PROFILE)

    def logout(self):
        self.click(self.LOGOUT_BTN)
