import os
import time
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, WebDriverException
from automation.utils.logger import get_logger

logger = get_logger("BasePage")

class BasePage:
    def __init__(self, driver, base_url=None):
        self.driver = driver
        self.base_url = base_url or "https://jayakrishna2005.github.io/BorrowBuddy"
        self.timeout = 10

    def navigate_to(self, path=""):
        url = f"{self.base_url.rstrip('/')}/{path.lstrip('/')}"
        logger.info(f"Navigating to URL: {url}")
        try:
            self.driver.get(url)
        except WebDriverException as e:
            logger.error(f"Navigation failed: {e}")
            raise e

    def find_element(self, locator):
        try:
            element = WebDriverWait(self.driver, self.timeout).until(
                EC.presence_of_element_located(locator)
            )
            return element
        except TimeoutException:
            logger.error(f"Element with locator {locator} not found within {self.timeout}s")
            self.capture_screenshot(f"fail_find_element_{int(time.time())}")
            raise

    def click(self, locator):
        try:
            element = WebDriverWait(self.driver, self.timeout).until(
                EC.element_to_be_clickable(locator)
            )
            element.click()
            logger.info(f"Clicked element: {locator}")
        except Exception as e:
            logger.error(f"Failed to click element {locator}: {e}")
            self.capture_screenshot(f"fail_click_{int(time.time())}")
            raise

    def enter_text(self, locator, text):
        try:
            element = self.find_element(locator)
            element.clear()
            element.send_keys(text)
            logger.info(f"Entered text '{text}' into element: {locator}")
        except Exception as e:
            logger.error(f"Failed to enter text in element {locator}: {e}")
            self.capture_screenshot(f"fail_text_{int(time.time())}")
            raise

    def get_text(self, locator):
        try:
            element = self.find_element(locator)
            text = element.text
            logger.info(f"Retrieved text '{text}' from element: {locator}")
            return text
        except Exception as e:
            logger.error(f"Failed to get text from element {locator}: {e}")
            raise

    def capture_screenshot(self, name):
        screenshot_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "screenshots"))
        os.makedirs(screenshot_dir, exist_ok=True)
        path = os.path.join(screenshot_dir, f"{name}.png")
        try:
            self.driver.save_screenshot(path)
            logger.info(f"Screenshot saved to: {path}")
            return path
        except Exception as e:
            logger.error(f"Failed to capture screenshot: {e}")
            return None
