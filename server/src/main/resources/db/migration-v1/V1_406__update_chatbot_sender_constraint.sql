-- Drop the old constraint that only allowed lowercase values
ALTER TABLE chatbot_message
    DROP CONSTRAINT chatbot_message_sender_check;

-- Update existing data from lowercase to uppercase
UPDATE chatbot_message
SET sender = UPPER(sender)
WHERE sender IN ('user', 'bot');

-- Add new constraint that allows uppercase values (matching Java enum)
ALTER TABLE chatbot_message
    ADD CONSTRAINT chatbot_message_sender_check
        CHECK (sender IN ('USER', 'BOT'));