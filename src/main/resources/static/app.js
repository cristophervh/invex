(function () {
    'use strict';

    const TOKEN_KEY = 'employee-service-token';
    const FIELDS = ['firstName', 'middleName', 'lastName', 'secondLastName', 'birthDate', 'age', 'gender', 'position'];

    const $ = (selector) => document.querySelector(selector);
    const loginView = $('#login-view');
    const appView = $('#app-view');
    const dialog = $('#employee-dialog');
    const form = $('#employee-form');

    let token = sessionStorage.getItem(TOKEN_KEY);
    let editingId = null;

    function decodeClaims(jwt) {
        try {
            const payload = jwt.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
            return JSON.parse(atob(payload));
        } catch (e) {
            return null;
        }
    }

    function isAdmin() {
        const claims = decodeClaims(token);
        return Boolean(claims && (claims.roles || []).includes('ADMIN'));
    }

    async function api(path, options = {}) {
        const headers = { 'Content-Type': 'application/json' };
        if (token) {
            headers.Authorization = 'Bearer ' + token;
        }
        const response = await fetch(path, { ...options, headers });
        if (response.status === 401 && path !== '/auth/token') {
            logout();
            throw new Error('Your session expired, please sign in again');
        }
        const body = response.status === 204 ? null : await response.json().catch(() => null);
        if (!response.ok) {
            const error = new Error((body && body.message) || 'Request failed with status ' + response.status);
            error.details = (body && body.validationErrors) || [];
            throw error;
        }
        return body;
    }

    function show(element, text) {
        element.textContent = text || '';
        element.hidden = !text;
    }

    function escapeHtml(value) {
        return String(value ?? '').replace(/[&<>"']/g, (c) => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        }[c]));
    }

    function fullName(e) {
        return [e.firstName, e.middleName, e.lastName, e.secondLastName].filter(Boolean).join(' ');
    }

    // ---- Session ----

    function renderSession() {
        const loggedIn = Boolean(token);
        loginView.hidden = loggedIn;
        appView.hidden = !loggedIn;
        $('#session').hidden = !loggedIn;
        if (!loggedIn) {
            return;
        }
        const claims = decodeClaims(token);
        $('#current-user').textContent = claims.sub + ' (' + (claims.roles || []).join(', ') + ')';
        document.querySelectorAll('[data-admin]').forEach((el) => { el.hidden = !isAdmin(); });
        loadEmployees();
    }

    function logout() {
        token = null;
        sessionStorage.removeItem(TOKEN_KEY);
        renderSession();
    }

    $('#login-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const data = Object.fromEntries(new FormData(event.target));
        try {
            const result = await api('/auth/token', { method: 'POST', body: JSON.stringify(data) });
            token = result.accessToken;
            sessionStorage.setItem(TOKEN_KEY, token);
            event.target.reset();
            show($('#login-error'));
            renderSession();
        } catch (e) {
            show($('#login-error'), e.message);
        }
    });

    $('#logout').addEventListener('click', logout);

    // ---- List and search ----

    async function loadEmployees(name) {
        show($('#list-error'));
        try {
            const path = name ? '/employees/search?name=' + encodeURIComponent(name) : '/employees';
            renderRows(await api(path));
        } catch (e) {
            show($('#list-error'), e.details.length ? e.details.map((d) => d.message).join(', ') : e.message);
        }
    }

    function renderRows(employees) {
        const admin = isAdmin();
        $('#employee-rows').innerHTML = employees.map((e) => `
            <tr>
                <td>${e.id}</td>
                <td>${escapeHtml(fullName(e))}</td>
                <td>${e.age}</td>
                <td>${e.gender}</td>
                <td>${e.birthDate}</td>
                <td>${escapeHtml(e.position)}</td>
                <td>${e.active ? 'Yes' : 'No'}</td>
                <td>${e.createdAt}</td>
                <td>${admin ? `<div class="row-actions">
                    <button class="small secondary" data-edit="${e.id}">Edit</button>
                    <button class="small danger" data-delete="${e.id}">Delete</button></div>` : ''}
                </td>
            </tr>`).join('');
        $('#empty').hidden = employees.length > 0;
        window.currentEmployees = employees;
    }

    $('#search-form').addEventListener('submit', (event) => {
        event.preventDefault();
        loadEmployees(event.target.name.value.trim());
    });

    $('#clear-search').addEventListener('click', () => {
        $('#search-form').reset();
        loadEmployees();
    });

    $('#employee-rows').addEventListener('click', async (event) => {
        const editId = event.target.dataset.edit;
        const deleteId = event.target.dataset.delete;
        if (editId) {
            openForm(window.currentEmployees.find((e) => String(e.id) === editId));
        }
        if (deleteId && confirm('Delete employee ' + deleteId + '?')) {
            try {
                await api('/employees/' + deleteId, { method: 'DELETE' });
                loadEmployees();
            } catch (e) {
                show($('#list-error'), e.message);
            }
        }
    });

    // ---- Create and edit ----

    function clearFieldErrors() {
        form.querySelectorAll('.field-error').forEach((el) => el.remove());
        form.querySelectorAll('.invalid').forEach((el) => el.classList.remove('invalid'));
        show($('#form-error'));
    }

    function openForm(employee) {
        editingId = employee ? employee.id : null;
        form.reset();
        clearFieldErrors();
        $('#dialog-title').textContent = employee ? 'Edit employee ' + employee.id : 'New employee';
        if (employee) {
            FIELDS.forEach((field) => { form.elements[field].value = employee[field] ?? ''; });
            form.elements.active.checked = employee.active;
        }
        dialog.showModal();
    }

    function readForm() {
        const data = {};
        FIELDS.forEach((field) => {
            const value = form.elements[field].value.trim();
            if (field === 'age') {
                data.age = value === '' ? null : Number(value);
            } else {
                data[field] = value === '' && field !== 'middleName' ? null : value;
            }
        });
        data.active = form.elements.active.checked;
        return data;
    }

    function showFieldErrors(error) {
        const general = [];
        error.details.forEach((detail) => {
            const field = detail.field.replace(/^\[\d+\]\.?/, '');
            const input = form.elements[field];
            if (input) {
                input.classList.add('invalid');
                const message = document.createElement('span');
                message.className = 'field-error';
                message.textContent = detail.message;
                input.closest('label').appendChild(message);
            } else {
                general.push(detail.message);
            }
        });
        show($('#form-error'), general.length || !error.details.length ? [error.message, ...general].join('. ') : '');
    }

    $('#new-employee').addEventListener('click', () => openForm(null));
    $('#cancel').addEventListener('click', () => dialog.close());

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        clearFieldErrors();
        const data = readForm();
        try {
            if (editingId) {
                await api('/employees/' + editingId, { method: 'PUT', body: JSON.stringify(data) });
            } else {
                await api('/employees', { method: 'POST', body: JSON.stringify(data) });
            }
            dialog.close();
            loadEmployees();
        } catch (e) {
            showFieldErrors(e);
        }
    });

    renderSession();
})();
