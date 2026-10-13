"use strict";

const API = "api";

const COLUMNS = [
    {sort: "id", title: "id", value: c => c.id},
    {sort: "name", title: "Название", value: c => c.name},
    {sort: "coordinatesX", title: "x", value: c => c.coordinatesX},
    {sort: "coordinatesY", title: "y", value: c => c.coordinatesY},
    {sort: "creationDate", title: "Создан", value: c => c.creationDate},
    {sort: "area", title: "Площадь", value: c => c.area},
    {sort: "population", title: "Население", value: c => c.population},
    {sort: "establishmentDate", title: "Основан", value: c => c.establishmentDate},
    {sort: "capital", title: "Столица", value: c => (c.capital ? "да" : "нет")},
    {sort: "metersAboveSeaLevel", title: "Высота", value: c => c.metersAboveSeaLevel},
    {sort: "populationDensity", title: "Плотность", value: c => c.populationDensity},
    {sort: "climate", title: "Климат", value: c => c.climate},
    {sort: "standardOfLiving", title: "Уровень жизни", value: c => c.standardOfLiving},
    {sort: "governor", title: "Губернатор", value: c => c.governorName}
];

const state = {
    page: 1,
    pageSize: 10,
    pageCount: 1,
    sortField: "id",
    ascending: true,
    filterField: "name",
    filterValue: "",
    editedCityId: null,
    deletion: null
};

async function request(method, path, body, query) {
    const url = new URL(API + path, document.baseURI);
    Object.entries(query || {}).forEach(([key, value]) => {
        if (value !== null && value !== undefined && value !== "") {
            url.searchParams.set(key, value);
        }
    });

    const response = await fetch(url, {
        method: method,
        headers: body === undefined ? {} : {"Content-Type": "application/json"},
        body: body === undefined ? undefined : JSON.stringify(body)
    });

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    const payload = text ? JSON.parse(text) : null;

    if (!response.ok) {
        throw {status: response.status, ...payload};
    }
    return payload;
}

function showMessage(text) {
    document.getElementById("message").textContent = text;
}

function clearFieldErrors(form) {
    form.querySelectorAll("[data-error-for]").forEach(node => (node.textContent = ""));
}

function showError(error, form) {
    if (form) {
        clearFieldErrors(form);
    }
    if (form && error.fieldErrors) {
        Object.entries(error.fieldErrors).forEach(([field, message]) => {
            const node = form.querySelector(`[data-error-for="${field}"]`);
            if (node) {
                node.textContent = message;
            }
        });
    }
    showMessage(error.message || "Не удалось выполнить операцию");
}

const VIEWS = ["cities", "city-form", "details", "references", "special"];

function showView(name) {
    VIEWS.forEach(view => {
        document.getElementById("view-" + view).hidden = view !== name;
    });
    showMessage("");
}

async function start() {
    try {
        const user = await request("GET", "/auth/me");
        enterApp(user.login);
    } catch (error) {
        document.getElementById("auth-screen").hidden = false;
        document.getElementById("app-screen").hidden = true;
    }
}

function enterApp(login) {
    document.getElementById("auth-screen").hidden = true;
    document.getElementById("app-screen").hidden = false;
    document.getElementById("current-user").textContent = login;
    connectWebSocket();
    showView("cities");
    loadCities();
}

document.getElementById("auth-form").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.target;
    const action = event.submitter && event.submitter.value === "register" ? "register" : "login";
    const credentials = {
        login: form.login.value,
        password: form.password.value
    };
    try {
        const user = await request("POST", "/auth/" + action, credentials);
        clearFieldErrors(form);
        showMessage("");
        enterApp(user.login);
    } catch (error) {
        showError(error, form);
    }
});

document.getElementById("logout-button").addEventListener("click", async () => {
    await request("POST", "/auth/logout");
    location.reload();
});

function connectWebSocket() {
    const protocol = location.protocol === "https:" ? "wss:" : "ws:";
    const socket = new WebSocket(`${protocol}//${location.host}${location.pathname}ws/cities`);

    socket.onmessage = () => {
        if (!document.getElementById("view-cities").hidden) {
            loadCities();
        }
        if (!document.getElementById("view-references").hidden) {
            loadReferences();
        }
    };
}

function renderCitiesHead() {
    const head = document.getElementById("cities-head");
    head.textContent = "";

    COLUMNS.forEach(column => {
        const cell = document.createElement("th");
        const button = document.createElement("button");
        button.type = "button";
        button.textContent = column.title + sortMarker(column.sort);
        button.addEventListener("click", () => sortBy(column.sort));
        cell.appendChild(button);
        head.appendChild(cell);
    });

    const actions = document.createElement("th");
    actions.textContent = "Действия";
    head.appendChild(actions);
}

function sortMarker(field) {
    if (field !== state.sortField) {
        return "";
    }
    return state.ascending ? " ↑" : " ↓";
}

function sortBy(field) {
    if (state.sortField === field) {
        state.ascending = !state.ascending;
    } else {
        state.sortField = field;
        state.ascending = true;
    }
    loadCities();
}

async function loadCities() {
    const page = await request("GET", "/cities", undefined, {
        filterField: state.filterField,
        filterValue: state.filterValue,
        sortField: state.sortField,
        ascending: state.ascending,
        page: state.page,
        pageSize: state.pageSize
    });

    state.pageCount = Math.max(1, Math.ceil(page.totalCount / state.pageSize));

    if (state.page > state.pageCount) {
        state.page = state.pageCount;
        return loadCities();
    }

    renderCitiesHead();

    const body = document.getElementById("cities-body");
    body.textContent = "";
    page.items.forEach(city => body.appendChild(cityRow(city)));

    document.getElementById("cities-empty").hidden = page.items.length > 0;
    document.getElementById("cities-table").hidden = page.items.length === 0;
    document.getElementById("pager-status").textContent =
        `Страница ${state.page} из ${state.pageCount}, всего объектов: ${page.totalCount}`;
}

function cityRow(city) {
    const row = document.createElement("tr");

    COLUMNS.forEach(column => {
        const cell = document.createElement("td");
        const value = column.value(city);
        cell.textContent = value === null || value === undefined ? "" : String(value);
        row.appendChild(cell);
    });

    const actions = document.createElement("td");
    actions.appendChild(button("просмотр", () => openDetails(city.id)));
    actions.appendChild(button("изменить", () => openCityForm(city.id)));
    actions.appendChild(button("удалить", () => deleteCity(city.id)));
    row.appendChild(actions);

    return row;
}

function button(text, handler) {
    const element = document.createElement("button");
    element.type = "button";
    element.textContent = text;
    element.addEventListener("click", handler);
    return element;
}

async function deleteCity(id) {
    if (!confirm("Удалить город?")) {
        return;
    }
    try {
        await request("DELETE", "/cities/" + id);
        showMessage("Город удалён");
        loadCities();
    } catch (error) {
        showError(error);
    }
}

document.getElementById("apply-filter").addEventListener("click", () => {
    state.filterField = document.getElementById("filter-field").value;
    state.filterValue = document.getElementById("filter-value").value;
    state.pageSize = Number(document.getElementById("page-size").value);
    state.page = 1;
    loadCities();
});

document.querySelectorAll("[data-page-step]").forEach(element => {
    element.addEventListener("click", () => {
        const step = element.dataset.pageStep;
        if (step === "first") state.page = 1;
        if (step === "prev") state.page = Math.max(1, state.page - 1);
        if (step === "next") state.page = Math.min(state.pageCount, state.page + 1);
        if (step === "last") state.page = state.pageCount;
        loadCities();
    });
});

document.querySelectorAll("nav [data-view]").forEach(element => {
    element.addEventListener("click", () => {
        const view = element.dataset.view;
        showView(view);
        if (view === "cities") loadCities();
        if (view === "references") loadReferences();
    });
});

document.getElementById("create-city").addEventListener("click", () => openCityForm(null));
document.getElementById("city-form-cancel").addEventListener("click", () => {
    showView("cities");
    loadCities();
});

async function openCityForm(id) {
    const form = document.getElementById("city-form");
    clearFieldErrors(form);
    state.editedCityId = id;

    const [coordinates, humans] = await Promise.all([
        request("GET", "/coordinates"),
        request("GET", "/humans")
    ]);

    fillOptions(form.coordinatesId, coordinates,
        item => `№${item.id} (${item.x}; ${item.y})`);
    fillOptions(form.governorId, humans,
        item => `№${item.id} ${item.name} (${item.age} лет)`);

    if (id === null) {
        document.getElementById("city-form-title").textContent = "Новый город";
        form.reset();
    } else {
        const city = await request("GET", "/cities/" + id);
        document.getElementById("city-form-title").textContent = "Изменение города №" + id;
        form.name.value = city.name ?? "";
        form.area.value = city.area ?? "";
        form.population.value = city.population ?? "";
        form.populationDensity.value = city.populationDensity ?? "";
        form.establishmentDate.value = city.establishmentDate ?? "";
        form.metersAboveSeaLevel.value = city.metersAboveSeaLevel ?? "";
        form.climate.value = city.climate ?? "";
        form.standardOfLiving.value = city.standardOfLiving ?? "";
        form.capital.checked = Boolean(city.capital);
        form.coordinatesId.value = city.coordinatesId ?? "";
        form.governorId.value = city.governorId ?? "";
    }

    showView("city-form");
}

function fillOptions(select, items, label) {
    select.textContent = "";

    const empty = document.createElement("option");
    empty.value = "";
    empty.textContent = "— не выбрано —";
    select.appendChild(empty);

    items.forEach(item => {
        const option = document.createElement("option");
        option.value = item.id;
        option.textContent = label(item);
        select.appendChild(option);
    });
}

document.getElementById("city-form").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.target;

    const dto = {
        name: form.name.value,
        coordinatesId: toNumber(form.coordinatesId.value),
        governorId: toNumber(form.governorId.value),
        area: toNumber(form.area.value),
        population: toNumber(form.population.value),
        populationDensity: toNumber(form.populationDensity.value),
        establishmentDate: form.establishmentDate.value || null,
        metersAboveSeaLevel: toNumber(form.metersAboveSeaLevel.value),
        climate: form.climate.value || null,
        standardOfLiving: form.standardOfLiving.value || null,
        capital: form.capital.checked
    };

    try {
        if (state.editedCityId === null) {
            await request("POST", "/cities", dto);
            showMessage("Город создан");
        } else {
            await request("PUT", "/cities/" + state.editedCityId, dto);
            showMessage("Город изменён");
        }
        clearFieldErrors(form);
        showView("cities");
        loadCities();
    } catch (error) {
        showError(error, form);
    }
});

function toNumber(value) {
    return value === "" || value === null ? null : Number(value);
}

document.getElementById("details-search").addEventListener("click", () => {
    openDetails(document.getElementById("details-id").value);
});

async function openDetails(id) {
    showView("details");
    document.getElementById("details-id").value = id ?? "";
    const result = document.getElementById("details-result");
    result.textContent = "";

    if (!id) {
        showMessage("Укажите идентификатор города");
        return;
    }

    try {
        const city = await request("GET", "/cities/" + id);
        result.appendChild(describe("Город", [
            ["id", city.id],
            ["Название", city.name],
            ["Дата создания", city.creationDate],
            ["Площадь", city.area],
            ["Население", city.population],
            ["Дата основания", city.establishmentDate],
            ["Столица", city.capital ? "да" : "нет"],
            ["Высота над уровнем моря", city.metersAboveSeaLevel],
            ["Плотность населения", city.populationDensity],
            ["Климат", city.climate],
            ["Уровень жизни", city.standardOfLiving]
        ]));
        result.appendChild(describe("Связанные объекты", [
            ["Координаты, id", city.coordinatesId],
            ["Координата x", city.coordinatesX],
            ["Координата y", city.coordinatesY],
            ["Губернатор, id", city.governorId],
            ["Имя губернатора", city.governorName],
            ["Возраст губернатора", city.governorAge]
        ]));
        result.appendChild(button("Изменить этот город", () => openCityForm(city.id)));
        showMessage("");
    } catch (error) {
        showError(error);
    }
}

function describe(title, rows) {
    const block = document.createElement("div");

    const heading = document.createElement("h3");
    heading.textContent = title;
    block.appendChild(heading);

    const table = document.createElement("table");
    table.border = "1";
    rows.forEach(([name, value]) => {
        const row = document.createElement("tr");

        const key = document.createElement("th");
        key.textContent = name;
        row.appendChild(key);

        const cell = document.createElement("td");
        cell.textContent = value === null || value === undefined ? "" : String(value);
        row.appendChild(cell);

        table.appendChild(row);
    });
    block.appendChild(table);

    return block;
}

let coordinatesCache = [];
let humansCache = [];

async function loadReferences() {
    [coordinatesCache, humansCache] = await Promise.all([
        request("GET", "/coordinates"),
        request("GET", "/humans")
    ]);

    fillReferenceTable("coordinates-body", coordinatesCache,
        item => [item.id, item.x, item.y], "coordinates");
    fillReferenceTable("humans-body", humansCache,
        item => [item.id, item.name, item.age, item.birthday ?? ""], "humans");
}

function fillReferenceTable(bodyId, items, cells, kind) {
    const body = document.getElementById(bodyId);
    body.textContent = "";

    items.forEach(item => {
        const row = document.createElement("tr");
        cells(item).forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = value === null || value === undefined ? "" : String(value);
            row.appendChild(cell);
        });

        const actions = document.createElement("td");
        actions.appendChild(button("изменить", () => editReference(kind, item)));
        actions.appendChild(button("удалить", () => prepareDeletion(kind, item.id)));
        row.appendChild(actions);

        body.appendChild(row);
    });
}

function editReference(kind, item) {
    if (kind === "coordinates") {
        const form = document.getElementById("coordinates-form");
        form.id.value = item.id;
        form.x.value = item.x;
        form.y.value = item.y;
    } else {
        const form = document.getElementById("human-form");
        form.id.value = item.id;
        form.name.value = item.name;
        form.age.value = item.age;
        form.birthday.value = item.birthday ?? "";
    }
}

document.getElementById("coordinates-reset").addEventListener("click", () => {
    document.getElementById("coordinates-form").reset();
});
document.getElementById("human-reset").addEventListener("click", () => {
    document.getElementById("human-form").reset();
});

document.getElementById("coordinates-form").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.target;
    const dto = {x: toNumber(form.x.value), y: toNumber(form.y.value)};

    try {
        if (form.id.value) {
            await request("PUT", "/coordinates/" + form.id.value, dto);
        } else {
            await request("POST", "/coordinates", dto);
        }
        clearFieldErrors(form);
        form.reset();
        showMessage("Координаты сохранены");
        loadReferences();
    } catch (error) {
        showError(error, form);
    }
});

document.getElementById("human-form").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.target;
    const dto = {
        name: form.name.value,
        age: toNumber(form.age.value),
        birthday: form.birthday.value || null
    };

    try {
        if (form.id.value) {
            await request("PUT", "/humans/" + form.id.value, dto);
        } else {
            await request("POST", "/humans", dto);
        }
        clearFieldErrors(form);
        form.reset();
        showMessage("Губернатор сохранён");
        loadReferences();
    } catch (error) {
        showError(error, form);
    }
});

async function prepareDeletion(kind, id) {
    const linked = await request("GET", `/${kind}/${id}/cities`);
    state.deletion = {kind: kind, id: id};

    document.getElementById("delete-title").textContent =
        (kind === "coordinates" ? "Удаление координат №" : "Удаление губернатора №") + id;

    const linkedBlock = document.getElementById("delete-linked");
    linkedBlock.textContent = "";

    const replacementRow = document.getElementById("delete-replacement-row");
    const replacementSelect = document.getElementById("delete-replacement");

    if (linked.length === 0) {
        linkedBlock.textContent = "Связанных городов нет, объект можно удалить сразу.";
        replacementRow.hidden = true;
    } else {
        const text = document.createElement("p");
        text.textContent = `На объект ссылаются города (${linked.length} шт.): `
            + linked.map(city => `№${city.id} ${city.name}`).join(", ")
            + ". Выберите объект, к которому они будут привязаны.";
        linkedBlock.appendChild(text);

        const source = kind === "coordinates" ? coordinatesCache : humansCache;
        const options = source.filter(item => item.id !== id);
        fillOptions(replacementSelect, options, item =>
            kind === "coordinates"
                ? `№${item.id} (${item.x}; ${item.y})`
                : `№${item.id} ${item.name}`);
        replacementRow.hidden = false;
    }

    document.getElementById("delete-dialog").hidden = false;
}

document.getElementById("delete-cancel").addEventListener("click", () => {
    state.deletion = null;
    document.getElementById("delete-dialog").hidden = true;
});

document.getElementById("delete-confirm").addEventListener("click", async () => {
    const replacement = document.getElementById("delete-replacement").value;
    try {
        await request("DELETE", `/${state.deletion.kind}/${state.deletion.id}`, undefined,
            {replacementId: replacement});
        showMessage("Объект удалён");
        state.deletion = null;
        document.getElementById("delete-dialog").hidden = true;
        loadReferences();
    } catch (error) {
        showError(error);
    }
});

document.getElementById("special-delete-climate").addEventListener("click", async () => {
    const climate = document.getElementById("special-climate").value;
    try {
        const city = await request("DELETE", "/special/by-climate", undefined, {climate: climate});
        showMessage(`Удалён город «${city.name}» (id ${city.id})`);
    } catch (error) {
        showError(error);
    }
});

document.getElementById("special-count").addEventListener("click", async () => {
    const meters = document.getElementById("special-meters").value;
    try {
        const result = await request("GET", "/special/count-by-meters", undefined,
            {metersAboveSeaLevel: meters});
        showMessage(`Городов на высоте ${meters}: ${result.count}`);
    } catch (error) {
        showError(error);
    }
});

document.getElementById("special-search").addEventListener("click", async () => {
    const part = document.getElementById("special-part").value;
    const result = document.getElementById("special-search-result");
    result.textContent = "";

    try {
        const cities = await request("GET", "/special/by-name-part", undefined, {part: part});
        showMessage(`Найдено городов: ${cities.length}`);

        const table = document.createElement("table");
        table.border = "1";

        const head = document.createElement("tr");
        ["id", "Название", "Население", "Климат"].forEach(title => {
            const cell = document.createElement("th");
            cell.textContent = title;
            head.appendChild(cell);
        });
        table.appendChild(head);

        cities.forEach(city => {
            const row = document.createElement("tr");
            [city.id, city.name, city.population, city.climate ?? ""].forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = String(value);
                row.appendChild(cell);
            });
            table.appendChild(row);
        });
        result.appendChild(table);
    } catch (error) {
        showError(error);
    }
});

document.getElementById("special-move").addEventListener("click", async () => {
    const sourceId = document.getElementById("special-source").value;
    const targetId = document.getElementById("special-target").value;
    try {
        const city = await request("POST", "/special/move-population", undefined,
            {sourceId: sourceId, targetId: targetId});
        showMessage(`Население переселено в «${city.name}», теперь там ${city.population} жителей.`
            + " Опустевший город удалён.");
    } catch (error) {
        showError(error);
    }
});

document.getElementById("special-move-smallest").addEventListener("click", async () => {
    const sourceId = document.getElementById("special-source").value;
    try {
        const city = await request("POST", "/special/move-population-to-smallest", undefined,
            {sourceId: sourceId});
        showMessage(`Население переселено в «${city.name}», теперь там ${city.population} жителей.`
            + " Опустевший город удалён.");
    } catch (error) {
        showError(error);
    }
});

start();
