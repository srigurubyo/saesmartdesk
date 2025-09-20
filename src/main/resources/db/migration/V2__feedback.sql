CREATE TABLE feedback (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL REFERENCES requests(id) ON DELETE CASCADE,
    given_by_user_id UUID NOT NULL REFERENCES users(id),
    given_by_role VARCHAR(40) NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments VARCHAR(4000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_feedback_request ON feedback(request_id);
CREATE INDEX idx_feedback_user ON feedback(given_by_user_id);
CREATE INDEX idx_feedback_created ON feedback(created_at);
