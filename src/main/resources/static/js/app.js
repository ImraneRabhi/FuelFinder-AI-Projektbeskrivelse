
const searchForm = document.getElementById("searchForm");
const resultStatus = document.getElementById("resultStatus");
const stationList = document.getElementById("stationList");

const aiForm = document.getElementById("aiForm");
const aiQuestion = document.getElementById("aiQuestion");
const aiAnswer = document.getElementById("aiAnswer");

const locationInput = document.getElementById("location");
const consumptionInput =
    document.getElementById("consumptionPer100Km");
const remainingFuelInput =
    document.getElementById("remainingFuelLiters");
const tankCapacityInput =
    document.getElementById("tankCapacityLiters");

let lastResult = null;

// Hent valgt søgeradius
function getSelectedRadius() {
    const selected = document.querySelector(
        'input[name="radius"]:checked'
    );

    const radius = selected ? Number(selected.value) : 5;

    if (![5, 10, 20].includes(radius)) {
        throw new Error("Vælg en søgeradius på 5, 10 eller 20 km.");
    }

    return radius;
}

// Hent valgt brændstoftype
function getFuelType() {
    return document.querySelector(
        'input[name="fuelType"]:checked'
    ).value;
}

// Byg request til Java-backend
function buildRequest(question) {
    const location = locationInput.value.trim();
    const consumption = Number(consumptionInput.value);
    const remainingFuel = Number(remainingFuelInput.value);
    const tankCapacity = Number(tankCapacityInput.value);

    if (!location) {
        throw new Error("Indtast din adresse eller placering.");
    }

    if (!consumptionInput.value ||
        !Number.isFinite(consumption) ||
        consumption <= 0) {
        throw new Error("Indtast et gyldigt brændstofforbrug.");
    }

    if (!remainingFuelInput.value ||
        !Number.isFinite(remainingFuel) ||
        remainingFuel < 0) {
        throw new Error("Indtast resterende brændstof.");
    }

    if (!tankCapacityInput.value ||
        !Number.isFinite(tankCapacity) ||
        tankCapacity <= 0) {
        throw new Error("Indtast en gyldig tankkapacitet.");
    }

    if (remainingFuel > tankCapacity) {
        throw new Error(
            "Resterende brændstof må ikke overstige tankkapaciteten."
        );
    }

    return {
        question: question,
        location: location,
        fuelType: getFuelType(),
        consumptionPer100Km: consumption,
        remainingFuelLiters: remainingFuel,
        tankCapacityLiters: tankCapacity,
        radiusKm: getSelectedRadius()
    };
}

// Send request til backend
async function fetchRecommendation(question) {
    const request = buildRequest(question);

    const response = await fetch("/api/fuelfinder/askai", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(request)
    });

    if (!response.ok) {
        let message = "Serverfejl: " + response.status;

        try {
            const error = await response.json();
            message = error.message || error.detail ||
                error.error || message;
        } catch {
            // Behold den oprindelige fejlbesked
        }

        throw new Error(message);
    }

    return await response.json();
}

// Søg efter tankstationer
searchForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    resultStatus.textContent = "Finder tankstationer...";
    stationList.replaceChildren();
    aiAnswer.textContent = "";
    lastResult = null;

    try {
        const result = await fetchRecommendation(
            "Find de nærmeste og billigste tankstationer."
        );

        lastResult = result;
        showStations(result);

    } catch (error) {
        resultStatus.textContent = error.message;
        console.error(error);
    }
});

// Vis tankstationer
function showStations(result) {
    stationList.replaceChildren();

    // Matcher FuelRecommendation fra Java-backend
    const stations = Array.isArray(result.stations)
        ? result.stations
        : [];

    if (stations.length === 0) {
        resultStatus.textContent =
            "Ingen tankstationer fundet inden for søgeradius.";
        return;
    }

    resultStatus.textContent =
        "Tankstationer fundet inden for søgeradius.";

    addSectionHeading("De 5 nærmeste tankstationer");

    stations.forEach((station, index) => {
        addStation(station, index + 1);
    });
}

// Tilføj overskrift
function addSectionHeading(title) {
    const li = document.createElement("li");
    const heading = document.createElement("h3");

    heading.textContent = title;
    li.appendChild(heading);

    stationList.appendChild(li);
}

// Tilføj tankstation
function addStation(station, number) {
    const li = document.createElement("li");

    const name = document.createElement("strong");
    name.textContent = number + ". " + station.name;

    const address = document.createElement("p");
    address.textContent = station.address || "";

    const price = document.createElement("p");
    price.textContent =
        "Pris: " + formatPrice(station.price) + " kr./liter";

    const distance = document.createElement("p");
    distance.textContent =
        "Køreafstand: " +
        formatDistance(station.distanceKm) + " km";

    li.appendChild(name);
    li.appendChild(address);
    li.appendChild(price);
    li.appendChild(distance);

    // Google Maps-rutevejledning
    if (station.directionsUrl) {
        const link = document.createElement("a");

        link.href = station.directionsUrl;
        link.textContent = "Vis rute i Google Maps";
        link.target = "_blank";
        link.rel = "noopener noreferrer";

        li.appendChild(link);
    }

    stationList.appendChild(li);
}

// Spørg FuelFinder AI
aiForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const question = aiQuestion.value.trim();

    if (!question) {
        aiAnswer.textContent = "Skriv et spørgsmål først.";
        return;
    }

    aiAnswer.textContent = "FuelFinder AI tænker...";

    try {
        const result = await fetchRecommendation(question);

        lastResult = result;
        showStations(result);
        showAiRecommendation(result);

    } catch (error) {
        aiAnswer.textContent = error.message;
        console.error(error);
    }
});

// Vis AI-anbefaling
function showAiRecommendation(result) {
    aiAnswer.replaceChildren();

    if (!result.recommendedStation) {
        aiAnswer.textContent =
            result.reason || "Ingen station kunne anbefales.";
        return;
    }

    const heading = document.createElement("strong");
    heading.textContent =
        "Anbefalet station: " + result.recommendedStation;

    const details = document.createElement("p");

    const priceText = result.price == null
        ? "Pris: Ukendt"
        : "Pris: " + formatPrice(result.price) + " kr./liter";

    const distanceText = result.distance == null
        ? "Køreafstand: Ukendt"
        : "Køreafstand: " +
        formatDistance(result.distance) + " km";

    const savingsText = result.totalSavings == null
        ? "Estimeret besparelse: Ikke beregnet"
        : "Estimeret besparelse: " +
        formatPrice(result.totalSavings) + " kr.";

    details.textContent =
        priceText + "\n" +
        distanceText + "\n" +
        savingsText;

    details.style.whiteSpace = "pre-line";

    const reason = document.createElement("p");
    reason.textContent = result.reason || "";

    aiAnswer.appendChild(heading);
    aiAnswer.appendChild(details);
    aiAnswer.appendChild(reason);
}

// Formater pris
function formatPrice(value) {
    if (value == null || !Number.isFinite(Number(value))) {
        return "Ukendt";
    }

    return Number(value).toLocaleString("da-DK", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

// Formater køreafstand
function formatDistance(value) {
    if (value == null || !Number.isFinite(Number(value))) {
        return "Ukendt";
    }

    return Number(value).toLocaleString("da-DK", {
        minimumFractionDigits: 1,
        maximumFractionDigits: 3
    });
}
