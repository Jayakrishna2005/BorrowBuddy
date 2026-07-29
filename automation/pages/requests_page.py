from selenium.webdriver.common.by import By
from automation.pages.base_page import BasePage

class RequestsPage(BasePage):
    # Locators
    TAB_RECEIVED = (By.XPATH, "//button[contains(text(), 'Received')]")
    TAB_SENT = (By.XPATH, "//button[contains(text(), 'Sent')]")
    MARK_RETURNED_BTN = (By.XPATH, "//button[contains(text(), 'Mark Returned')]")
    OPEN_CHAT_BTN = (By.XPATH, "//button[contains(text(), 'Open Chat')]")
    APPROVE_BTN = (By.XPATH, "//button[contains(text(), 'Approve')]")
    REJECT_BTN = (By.XPATH, "//button[contains(text(), 'Reject')]")

    def __init__(self, driver, base_url=None):
        super().__init__(driver, base_url)

    def switch_to_received_requests(self):
        self.click(self.TAB_RECEIVED)

    def switch_to_sent_requests(self):
        self.click(self.TAB_SENT)

    def click_mark_returned(self):
        self.click(self.MARK_RETURNED_BTN)

    def click_open_chat(self):
        self.click(self.OPEN_CHAT_BTN)

    def click_approve(self):
        self.click(self.APPROVE_BTN)

    def click_reject(self):
        self.click(self.REJECT_BTN)
