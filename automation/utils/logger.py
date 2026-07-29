import os
import logging

def get_logger(name="Automation"):
    logger = logging.getLogger(name)
    if not logger.handlers:
        logger.setLevel(logging.INFO)
        
        # Log directory path
        log_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "logs"))
        os.makedirs(log_dir, exist_ok=True)
        log_file = os.path.join(log_dir, f"{name.lower()}.log")

        formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

        # File Handler
        fh = logging.FileHandler(log_file, encoding='utf-8')
        fh.setLevel(logging.INFO)
        fh.setFormatter(formatter)
        logger.addHandler(fh)

        # Console Handler
        ch = logging.StreamHandler()
        ch.setLevel(logging.INFO)
        ch.setFormatter(formatter)
        logger.addHandler(ch)

    return logger
