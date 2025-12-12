-- Add column to store referenced FAQ IDs as JSON array
ALTER TABLE chatbot_message
ADD COLUMN referenced_faq_ids JSONB;

-- Add index for FAQ ID queries (for analytics)
CREATE INDEX chatbot_message_referenced_faq_ids_idx 
ON chatbot_message USING GIN (referenced_faq_ids);

-- Add comment explaining the column
COMMENT ON COLUMN chatbot_message.referenced_faq_ids IS 
'Array of FAQ IDs that were referenced in generating this bot response. Used for tracking which FAQs are most commonly used.';
