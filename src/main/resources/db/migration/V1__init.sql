CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    full_name VARCHAR(160) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    mfa_totp_secret VARCHAR(120),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE approval_workflows (
    id UUID PRIMARY KEY,
    request_type VARCHAR(60) NOT NULL,
    version INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE approval_steps (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL REFERENCES approval_workflows(id) ON DELETE CASCADE,
    step_order INT NOT NULL,
    approver_role VARCHAR(40) NOT NULL,
    sla_hours INT NOT NULL,
    UNIQUE (workflow_id, step_order)
);

CREATE TABLE requests (
    id UUID PRIMARY KEY,
    request_type VARCHAR(60) NOT NULL,
    detail_id UUID NOT NULL,
    requester_id UUID NOT NULL REFERENCES users(id),
    current_step INT,
    status VARCHAR(30) NOT NULL,
    workflow_id UUID NOT NULL REFERENCES approval_workflows(id),
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    submitted_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    due_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_requests_request_type ON requests(request_type);
CREATE INDEX idx_requests_status ON requests(status);
CREATE INDEX idx_requests_requester ON requests(requester_id);

CREATE TABLE request_approvals (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL REFERENCES requests(id) ON DELETE CASCADE,
    step_order INT NOT NULL,
    approver_id UUID REFERENCES users(id),
    decision VARCHAR(20),
    comment VARCHAR(2000),
    decided_at TIMESTAMPTZ,
    UNIQUE(request_id, step_order)
);

CREATE INDEX idx_request_approvals_request ON request_approvals(request_id);

CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    actor_id UUID REFERENCES users(id),
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(60) NOT NULL,
    entity_id UUID NOT NULL,
    at TIMESTAMPTZ NOT NULL DEFAULT now(),
    details VARCHAR(4000)
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    request_id UUID REFERENCES requests(id) ON DELETE CASCADE,
    channel VARCHAR(20) NOT NULL,
    template_key VARCHAR(80) NOT NULL,
    payload VARCHAR(4000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at TIMESTAMPTZ
);

CREATE TABLE halls (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    location VARCHAR(120) NOT NULL,
    capacity INT NOT NULL
);

CREATE TABLE hall_bookings (
    id UUID PRIMARY KEY,
    hall_id UUID NOT NULL REFERENCES halls(id),
    start_datetime TIMESTAMPTZ NOT NULL,
    end_datetime TIMESTAMPTZ NOT NULL,
    layout VARCHAR(120),
    participant_count INT NOT NULL,
    equipment_list TEXT,
    purpose VARCHAR(500)
);

CREATE TABLE defect_reports (
    id UUID PRIMARY KEY,
    building VARCHAR(160) NOT NULL,
    defect_type VARCHAR(120) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    description VARCHAR(2000) NOT NULL,
    photo_urls TEXT,
    reported_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO roles (name) VALUES ('REQUESTOR'), ('HOD'), ('ADMIN'), ('UNIT_OFFICER');

INSERT INTO users (id, username, password_hash, email, full_name)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'requestor1', '$2a$10$7eqJtq98hPqEX7fNZaFWoOHiDgEZe2r9Vm1KMgGa/T7hZqB6m0Z7W', 'requestor1@example.com', 'Requestor One'),
    ('00000000-0000-0000-0000-000000000002', 'requestor2', '$2a$10$7eqJtq98hPqEX7fNZaFWoOHiDgEZe2r9Vm1KMgGa/T7hZqB6m0Z7W', 'requestor2@example.com', 'Requestor Two'),
    ('00000000-0000-0000-0000-000000000003', 'hod1', '$2a$10$7eqJtq98hPqEX7fNZaFWoOHiDgEZe2r9Vm1KMgGa/T7hZqB6m0Z7W', 'hod1@example.com', 'Head Of Dept One'),
    ('00000000-0000-0000-0000-000000000004', 'hod2', '$2a$10$7eqJtq98hPqEX7fNZaFWoOHiDgEZe2r9Vm1KMgGa/T7hZqB6m0Z7W', 'hod2@example.com', 'Head Of Dept Two'),
    ('00000000-0000-0000-0000-000000000005', 'admin1', '$2a$10$7eqJtq98hPqEX7fNZaFWoOHiDgEZe2r9Vm1KMgGa/T7hZqB6m0Z7W', 'admin1@example.com', 'Admin One'),
    ('00000000-0000-0000-0000-000000000006', 'admin2', '$2a$10$7eqJtq98hPqEX7fNZaFWoOHiDgEZe2r9Vm1KMgGa/T7hZqB6m0Z7W', 'admin2@example.com', 'Admin Two'),
    ('00000000-0000-0000-0000-000000000007', 'unit1', '$2a$10$7eqJtq98hPqEX7fNZaFWoOHiDgEZe2r9Vm1KMgGa/T7hZqB6m0Z7W', 'unit1@example.com', 'Unit Officer One'),
    ('00000000-0000-0000-0000-000000000008', 'unit2', '$2a$10$7eqJtq98hPqEX7fNZaFWoOHiDgEZe2r9Vm1KMgGa/T7hZqB6m0Z7W', 'unit2@example.com', 'Unit Officer Two');

INSERT INTO user_roles(user_id, role_id)
SELECT users.id, roles.id
FROM users, roles
WHERE (users.username LIKE 'requestor%' AND roles.name = 'REQUESTOR')
   OR (users.username LIKE 'hod%' AND roles.name = 'HOD')
   OR (users.username LIKE 'admin%' AND roles.name = 'ADMIN')
   OR (users.username LIKE 'unit%' AND roles.name = 'UNIT_OFFICER');

INSERT INTO halls(id, name, location, capacity)
VALUES ('33333333-3333-3333-3333-333333333333', 'Main Conference Hall', 'Building A', 120);

INSERT INTO approval_workflows(id, request_type, version, active)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'HALL_BOOKING', 1, TRUE),
    ('22222222-2222-2222-2222-222222222222', 'DEFECT_REPORT', 1, TRUE);

INSERT INTO approval_steps(id, workflow_id, step_order, approver_role, sla_hours)
VALUES
    ('11111111-1111-1111-1111-111111111112', '11111111-1111-1111-1111-111111111111', 1, 'HOD', 24),
    ('11111111-1111-1111-1111-111111111113', '11111111-1111-1111-1111-111111111111', 2, 'ADMIN', 24),
    ('22222222-2222-2222-2222-222222222223', '22222222-2222-2222-2222-222222222222', 1, 'HOD', 24),
    ('22222222-2222-2222-2222-222222222224', '22222222-2222-2222-2222-222222222222', 2, 'UNIT_OFFICER', 24);
