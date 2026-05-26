"""
Server Flask SkillSwap School — fase W1 (setup minimo).
Avvio da questa cartella: python app.py
"""
from flask import Flask, jsonify, request, abort
from flask_cors import CORS
from config import HOST, PORT, SECRET_KEY, DATA_DIR

import csv
import os
from datetime import datetime
import re

app = Flask(__name__)
app.secret_key = SECRET_KEY
CORS(app)


def _csv_path(filename: str) -> str:
    return os.path.join(DATA_DIR, filename)


def _read_csv(filename: str):
    path = _csv_path(filename)
    if not os.path.exists(path):
        return []
    with open(path, newline='', encoding='utf-8') as f:
        reader = csv.reader(f, delimiter=';')
        return [row for row in reader if row]


def _write_csv_atomic(filename: str, rows):
    path = _csv_path(filename)
    tmp = path + '.tmp'
    with open(tmp, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerows(rows)
    os.replace(tmp, path)


def _next_id(prefix: str, existing_ids):
    nums = []
    for i in existing_ids:
        m = re.match(rf"{re.escape(prefix)}(\d+)$", i)
        if m:
            nums.append(int(m.group(1)))
    n = max(nums) + 1 if nums else 1
    return f"{prefix}{n}"


@app.route('/')
def index():
    return 'SkillSwap School — server attivo'


@app.route('/api/skills')
def api_skills():
    rows = _read_csv('skills.csv')
    skills = [{'skillId': r[0], 'name': r[1], 'type': r[2]} for r in rows]
    return jsonify(skills)


@app.route('/api/students')
def api_students():
    rows = _read_csv('students.csv')
    students = []
    for r in rows:
        # id;name;classe;email;ratingAvg;ratingCount;[passwordHash]
        s = {
            'studentId': r[0],
            'name': r[1],
            'class': r[2],
            'email': r[3],
            'ratingAvg': float(r[4]) if r[4] else 0.0,
            'ratingCount': int(r[5]) if r[5] else 0
        }
        students.append(s)
    return jsonify(students)


@app.route('/api/offers')
def api_offers():
    rows = _read_csv('offers.csv')
    offers = []
    for r in rows:
        # O1;S2;K1;INTERMEDIATE;desc;true
        offers.append({
            'offerId': r[0],
            'studentId': r[1],
            'skillId': r[2],
            'skillLevel': r[3],
            'description': r[4],
            'active': r[5].lower() == 'true'
        })
    studentId = request.args.get('studentId')
    active = request.args.get('active')
    if studentId:
        offers = [o for o in offers if o['studentId'] == studentId]
    if active is not None:
        val = active.lower() in ('1', 'true', 'yes')
        offers = [o for o in offers if o['active'] == val]
    return jsonify(offers)


@app.route('/api/requests')
def api_requests():
    rows = _read_csv('requests.csv')
    reqs = []
    for r in rows:
        # R1;S1;K1;BEGINNER;desc
        reqs.append({
            'requestId': r[0],
            'studentId': r[1],
            'skillId': r[2],
            'skillLevel': r[3],
            'description': r[4]
        })
    studentId = request.args.get('studentId')
    if studentId:
        reqs = [q for q in reqs if q['studentId'] == studentId]
    return jsonify(reqs)


@app.route('/api/exchanges')
def api_exchanges():
    rows = _read_csv('exchanges.csv')
    exchanges = []
    for r in rows:
        # E1;O2;R2;STATUS;createdAt;completedAt
        exchanges.append({
            'exchangeId': r[0],
            'offerId': r[1],
            'requestId': r[2],
            'status': r[3],
            'createdAt': r[4] if len(r) > 4 else None,
            'completedAt': r[5] if len(r) > 5 else None
        })
    studentId = request.args.get('studentId')
    if studentId:
        exchanges = [e for e in exchanges if _exchange_has_student(e, studentId)]
    return jsonify(exchanges)


def _exchange_has_student(exchange, studentId):
    # check owner of offer or requester
    offers = _read_csv('offers.csv')
    offer_map = {r[0]: r[1] for r in offers}
    requests = _read_csv('requests.csv')
    req_map = {r[0]: r[1] for r in requests}
    return offer_map.get(exchange['offerId']) == studentId or req_map.get(exchange['requestId']) == studentId


@app.route('/api/exchanges', methods=['POST'])
def api_exchanges_post():
    body = request.get_json() or {}
    offerId = body.get('offerId')
    requestId = body.get('requestId')
    if not offerId or not requestId:
        abort(400, 'offerId and requestId are required')
    rows = _read_csv('exchanges.csv')
    existing_ids = [r[0] for r in rows]
    exchangeId = body.get('exchangeId') or _next_id('E', existing_ids)
    createdAt = datetime.utcnow().isoformat()
    new_row = [exchangeId, offerId, requestId, 'PROPOSED', createdAt, '']
    rows.append(new_row)
    _write_csv_atomic('exchanges.csv', rows)
    return jsonify({'exchangeId': exchangeId}), 201


def _update_exchange_status(exchangeId, new_status):
    rows = _read_csv('exchanges.csv')
    found = False
    now = datetime.utcnow().isoformat()
    for r in rows:
        if r[0] == exchangeId:
            found = True
            r[3] = new_status
            # ensure createdAt exists
            if len(r) < 5 or not r[4]:
                r[4] = now
            # set completedAt for finished states
            if new_status in ('COMPLETED', 'CANCELLED'):
                if len(r) < 6:
                    r.extend([''] * (6 - len(r)))
                r[5] = now
            break
    if not found:
        abort(404, 'exchange not found')
    _write_csv_atomic('exchanges.csv', rows)


@app.route('/api/exchanges/<exchangeId>/accept', methods=['PUT'])
def api_exchange_accept(exchangeId):
    _update_exchange_status(exchangeId, 'ACCEPTED')
    return ('', 204)


@app.route('/api/exchanges/<exchangeId>/complete', methods=['PUT'])
def api_exchange_complete(exchangeId):
    _update_exchange_status(exchangeId, 'COMPLETED')
    return ('', 204)


@app.route('/api/exchanges/<exchangeId>/cancel', methods=['PUT'])
def api_exchange_cancel(exchangeId):
    _update_exchange_status(exchangeId, 'CANCELLED')
    return ('', 204)


@app.route('/api/reviews', methods=['GET', 'POST'])
def api_reviews():
    if request.method == 'GET':
        rows = _read_csv('reviews.csv')
        reviews = []
        for r in rows:
            # V1;E1;S2;S1;5;comment;timestamp
            reviews.append({
                'reviewId': r[0],
                'exchangeId': r[1],
                'reviewerStudentId': r[2],
                'revieweeStudentId': r[3],
                'stars': int(r[4]) if r[4] else None,
                'comment': r[5] if len(r) > 5 else '',
                'timestamp': r[6] if len(r) > 6 else None
            })
        return jsonify(reviews)

    # POST
    body = request.get_json() or {}
    exchangeId = body.get('exchangeId')
    reviewer = body.get('reviewerStudentId')
    reviewee = body.get('revieweeStudentId')
    stars = body.get('stars')
    comment = body.get('comment', '')
    if not (exchangeId and reviewer and reviewee and stars is not None):
        abort(400, 'exchangeId, reviewerStudentId, revieweeStudentId and stars are required')
    rows = _read_csv('reviews.csv')
    existing_ids = [r[0] for r in rows]
    reviewId = body.get('reviewId') or _next_id('V', existing_ids)
    now = datetime.utcnow().isoformat()
    new_row = [reviewId, exchangeId, reviewer, reviewee, str(int(stars)), comment, now]
    rows.append(new_row)
    _write_csv_atomic('reviews.csv', rows)
    # update student ratings
    _recalc_student_ratings()
    return jsonify({'reviewId': reviewId}), 201


def _recalc_student_ratings():
    reviews = _read_csv('reviews.csv')
    by_student = {}
    for r in reviews:
        if len(r) < 5:
            continue
        reviewee = r[3]
        stars = int(r[4]) if r[4] else 0
        by_student.setdefault(reviewee, []).append(stars)
    students = _read_csv('students.csv')
    out = []
    for s in students:
        sid = s[0]
        ratings = by_student.get(sid, [])
        if ratings:
            avg = sum(ratings) / len(ratings)
            s[4] = f"{avg:.1f}"
            s[5] = str(len(ratings))
        else:
            s[4] = '0.0'
            s[5] = '0'
        out.append(s)
    _write_csv_atomic('students.csv', out)


@app.route('/api/offers', methods=['POST'])
def api_offers_post():
    body = request.get_json() or {}
    studentId = body.get('studentId')
    skillId = body.get('skillId')
    skillLevel = body.get('skillLevel')
    description = body.get('description', '')
    active = body.get('active', True)
    if not (studentId and skillId and skillLevel):
        abort(400, 'studentId, skillId and skillLevel are required')
    rows = _read_csv('offers.csv')
    existing_ids = [r[0] for r in rows]
    offerId = body.get('offerId') or _next_id('O', existing_ids)
    new_row = [offerId, studentId, skillId, skillLevel, description, 'true' if active else 'false']
    rows.append(new_row)
    _write_csv_atomic('offers.csv', rows)
    return jsonify({'offerId': offerId}), 201


@app.route('/api/requests', methods=['POST'])
def api_requests_post():
    body = request.get_json() or {}
    studentId = body.get('studentId')
    skillId = body.get('skillId')
    skillLevel = body.get('skillLevel')
    description = body.get('description', '')
    if not (studentId and skillId and skillLevel):
        abort(400, 'studentId, skillId and skillLevel are required')
    rows = _read_csv('requests.csv')
    existing_ids = [r[0] for r in rows]
    requestId = body.get('requestId') or _next_id('R', existing_ids)
    new_row = [requestId, studentId, skillId, skillLevel, description]
    rows.append(new_row)
    _write_csv_atomic('requests.csv', rows)
    return jsonify({'requestId': requestId}), 201


@app.route('/api/students/<studentId>/reviews')
def api_student_reviews(studentId):
    rows = _read_csv('reviews.csv')
    reviews = []
    for r in rows:
        if len(r) > 3 and r[3] == studentId:
            reviews.append({
                'reviewId': r[0],
                'exchangeId': r[1],
                'reviewerStudentId': r[2],
                'revieweeStudentId': r[3],
                'stars': int(r[4]) if r[4] else None,
                'comment': r[5] if len(r) > 5 else '',
                'timestamp': r[6] if len(r) > 6 else None
            })
    return jsonify(reviews)


@app.route('/api/ranking')
def api_ranking():
    students = _read_csv('students.csv')
    ranked = []
    for s in students:
        try:
            avg = float(s[4])
            count = int(s[5])
        except Exception:
            avg = 0.0
            count = 0
        if count > 0:
            ranked.append({'studentId': s[0], 'name': s[1], 'ratingAvg': avg, 'ratingCount': count})
    ranked.sort(key=lambda x: x['ratingAvg'], reverse=True)
    return jsonify(ranked)


@app.route('/api/students/<studentId>/matches/one-way')
def api_matches_one_way(studentId):
    # Placeholder: lightweight matching based on requests/offers overlap
    # Return empty list for now to keep API stable.
    return jsonify([])


@app.route('/api/students/<studentId>/matches/swap')
def api_matches_swap(studentId):
    return jsonify([])


if __name__ == '__main__':
    app.run(host=HOST, port=PORT, debug=True)
