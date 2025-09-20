-- Auto-loaded dev dataset for SmartDesk.
-- Runs on startup when the dev profile is active.

-- Clean pre-existing sample data -------------------------------------------------
DELETE FROM audit_log WHERE id IN (
    '77777777-8888-4999-aaaa-bbbbbbbbbbbb',
    '88888888-9999-4aaa-bbbb-cccccccccccc'
);
DELETE FROM request_approvals WHERE request_id IN (
    'aa111111-2222-3333-4444-555555555555',
    'bb111111-2222-3333-4444-666666666666',
    'cc111111-2222-3333-4444-777777777777'
);
DELETE FROM requests WHERE id IN (
    'aa111111-2222-3333-4444-555555555555',
    'bb111111-2222-3333-4444-666666666666',
    'cc111111-2222-3333-4444-777777777777'
);
DELETE FROM hall_bookings WHERE id IN (
    '9f15c0c4-1c0d-4601-8134-8f4e24d902b1',
    'c7d1789a-8a78-4e6f-9e14-1a2f04b585dd'
);
DELETE FROM defect_reports WHERE id = 'd8fa10b2-501e-4799-b3d4-21dbd15c7d90';
DELETE FROM halls WHERE id IN (
    '44444444-4444-4444-4444-444444444441',
    '55555555-5555-5555-5555-555555555552'
);

-- Enrich reference data -----------------------------------------------------
INSERT INTO halls (id, name, location, capacity)
VALUES
    ('44444444-4444-4444-4444-444444444441', 'Executive Auditorium', 'Building B - Level 2', 220),
    ('55555555-5555-5555-5555-555555555552', 'Innovation Lab', 'Building C - Level 5', 60);

-- Hall booking requests -----------------------------------------------------
INSERT INTO hall_bookings (id, hall_id, start_datetime, end_datetime, layout, participant_count, equipment_list, purpose)
VALUES
    ('9f15c0c4-1c0d-4601-8134-8f4e24d902b1', '33333333-3333-3333-3333-333333333333', TIMESTAMP '2025-10-01 10:00:00+00', TIMESTAMP '2025-10-01 12:00:00+00', 'Boardroom', 18, 'Projector, Polycom, Whiteboard', 'Quarterly steering committee'),
    ('c7d1789a-8a78-4e6f-9e14-1a2f04b585dd', '44444444-4444-4444-4444-444444444441', TIMESTAMP '2025-09-26 14:00:00+00', TIMESTAMP '2025-09-26 18:00:00+00', 'Auditorium', 190, 'Stage lighting, PA system, Recording rig', 'Annual awards ceremony');

INSERT INTO requests (id, request_type, detail_id, requester_id, current_step, status, workflow_id, priority, submitted_at, updated_at, due_at, closed_at)
VALUES
    ('aa111111-2222-3333-4444-555555555555', 'HALL_BOOKING', '9f15c0c4-1c0d-4601-8134-8f4e24d902b1', '00000000-0000-0000-0000-000000000001', 2, 'PENDING_APPROVAL', '11111111-1111-1111-1111-111111111111', 'HIGH', TIMESTAMP '2025-09-20 07:30:00+00', TIMESTAMP '2025-09-20 09:05:00+00', TIMESTAMP '2025-09-21 09:05:00+00', NULL),
    ('bb111111-2222-3333-4444-666666666666', 'HALL_BOOKING', 'c7d1789a-8a78-4e6f-9e14-1a2f04b585dd', '00000000-0000-0000-0000-000000000002', NULL, 'APPROVED', '11111111-1111-1111-1111-111111111111', 'NORMAL', TIMESTAMP '2025-07-10 11:15:00+00', TIMESTAMP '2025-07-11 08:00:00+00', NULL, TIMESTAMP '2025-07-11 08:00:00+00');

INSERT INTO request_approvals (id, request_id, step_order, approver_id, decision, comment, decided_at)
VALUES
    ('1a1a1a1a-aaaa-4aaa-9aaa-1a1a1a1a1a10', 'aa111111-2222-3333-4444-555555555555', 1, '00000000-0000-0000-0000-000000000003', 'APPROVED', 'Schedule fits the academic calendar.', TIMESTAMP '2025-09-20 08:10:00+00'),
    ('2b2b2b2b-bbbb-4bbb-9bbb-2b2b2b2b2b20', 'aa111111-2222-3333-4444-555555555555', 2, NULL, NULL, NULL, NULL),
    ('3c3c3c3c-cccc-4ccc-9ccc-3c3c3c3c3c30', 'bb111111-2222-3333-4444-666666666666', 1, '00000000-0000-0000-0000-000000000004', 'APPROVED', 'Capacity verified.', TIMESTAMP '2025-07-10 13:45:00+00'),
    ('4d4d4d4d-dddd-4ddd-9ddd-4d4d4d4d4d40', 'bb111111-2222-3333-4444-666666666666', 2, '00000000-0000-0000-0000-000000000005', 'APPROVED', 'Event logged with facilities.', TIMESTAMP '2025-07-10 14:10:00+00');

-- Defect report request -----------------------------------------------------
INSERT INTO defect_reports (id, building, defect_type, severity, description, photo_urls, reported_at)
VALUES
    ('d8fa10b2-501e-4799-b3d4-21dbd15c7d90', 'Building A - Block 3', 'Air Conditioning', 'HIGH', 'HVAC unit on 3rd floor is leaking and causing condensation near electrical cabling.', 'https://cdn.example.com/defects/hvac-3f-01.jpg', TIMESTAMP '2025-09-18 05:20:00+00');

INSERT INTO requests (id, request_type, detail_id, requester_id, current_step, status, workflow_id, priority, submitted_at, updated_at, due_at, closed_at)
VALUES
    ('cc111111-2222-3333-4444-777777777777', 'DEFECT_REPORT', 'd8fa10b2-501e-4799-b3d4-21dbd15c7d90', '00000000-0000-0000-0000-000000000001', NULL, 'REJECTED', '22222222-2222-2222-2222-222222222222', 'URGENT', TIMESTAMP '2025-09-18 05:25:00+00', TIMESTAMP '2025-09-18 09:40:00+00', NULL, TIMESTAMP '2025-09-18 09:40:00+00');

INSERT INTO request_approvals (id, request_id, step_order, approver_id, decision, comment, decided_at)
VALUES
    ('5e5e5e5e-eeee-4eee-9eee-5e5e5e5e5e50', 'cc111111-2222-3333-4444-777777777777', 1, '00000000-0000-0000-0000-000000000003', 'APPROVED', 'Please engage maintenance immediately.', TIMESTAMP '2025-09-18 07:00:00+00'),
    ('6f6f6f6f-ffff-4fff-9fff-6f6f6f6f6f60', 'cc111111-2222-3333-4444-777777777777', 2, '00000000-0000-0000-0000-000000000007', 'REJECTED', 'Duplicate of incident INC-4821; tracking there.', TIMESTAMP '2025-09-18 09:35:00+00');

-- Optional: audit log snapshots for timelines --------------------------------
INSERT INTO audit_log (id, actor_id, action, entity_type, entity_id, at, details)
VALUES
    ('77777777-8888-4999-aaaa-bbbbbbbbbbbb', '00000000-0000-0000-0000-000000000003', 'REQUEST_APPROVE', 'REQUEST', 'aa111111-2222-3333-4444-555555555555', TIMESTAMP '2025-09-20 08:11:00+00', 'step=1'),
    ('88888888-9999-4aaa-bbbb-cccccccccccc', '00000000-0000-0000-0000-000000000007', 'REQUEST_REJECT', 'REQUEST', 'cc111111-2222-3333-4444-777777777777', TIMESTAMP '2025-09-18 09:36:00+00', 'step=2');
