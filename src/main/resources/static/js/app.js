
const searchForm = document.getElementById("searchForm");
const radiusInput = document.getElementById("radius");
const radiusValue = document.getElementById("radiusValue");
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

// Opdater søgeradius
radiusInput.addEventListener("input", () => {
    radiusValue.textContent = radiusInput.value;
});

// Hent valgt brændstoftype
function getFuelType() {
    return document.querySelector(
        'input[name="fuelType"]:checked'
    ).value;
}

// Byg request til Java-backend
function buildRequest(question) {
    if (!locationInput || !consumptionInput
        || !remainingFuelInput || !tankCapacityInput) {
        throw new Error(
            "HTML mangler felter til adresse og biloplysninger."
        );
    }

    const location = locationInput.value.trim();
    const consumption = Number(consumptionInput.value);
    const remainingFuel = Number(remainingFuelInput.value);
    const tankCapacity = Number(tankCapacityInput.value);
    const radius = Number(radiusInput.value);

    if (!location) {
        throw new Error("Indtast din adresse eller placering.");
    }

    if (!consumptionInput.value || consumption <= 0) {
        throw new Error("Indtast et gyldigt brændstofforbrug.");
    }

    if (!remainingFuelInput.value || remainingFuel < 0) {
        throw new Error("Indtast resterende brændstof.");
    }

    if (!tankCapacityInput.value || tankCapacity <= 0) {
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
        radiusKm: radius
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
            message = error.message || error.error || message;
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
    stationList.innerHTML = "";
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
    stationList.innerHTML = "";

    const nearest = result.nearestStations || [];
    const cheapest = result.cheapestStations || [];

    if (nearest.length === 0 && cheapest.length === 0) {
        resultStatus.textContent =
            "Ingen tankstationer fundet inden for søgeradius.";
        return;
    }

    resultStatus.textContent =
        "Tankstationer fundet inden for søgeradius.";

    addSectionHeading("De 5 nærmeste tankstationer");

    nearest.forEach((station, index) => {
        addStation(station, index + 1);
    });

    addSectionHeading("De 5 billigste tankstationer");

    cheapest.forEach((station, index) => {
        addStation(station, index + 1);
    });
}

// Tilføj overskrift til listen
function addSectionHeading(title) {
    const li = document.createElement("li");
    const heading = document.createElement("h3");

    heading.textContent = title;
    li.appendChild(heading);

    stationList.appendChild(li);
}

// Tilføj tankstation til listen
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

// Vis AI-anbefalingen
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
    details.textContent =
        "Pris: " + formatPrice(result.price) +
        " kr./liter\n" +
        "Køreafstand: " + formatDistance(result.distance) +
        " km\n" +
        "Estimeret besparelse: " +
        formatPrice(result.totalSavings) + " kr.";

    const reason = document.createElement("p");
    reason.textContent = result.reason || "";

    aiAnswer.appendChild(heading);
    aiAnswer.appendChild(details);
    aiAnswer.appendChild(reason);
}

// Formater pris til dansk format
function formatPrice(value) {
    if (value == null) return "Ukendt";

    return Number(value).toLocaleString("da-DK", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

// Formater afstand
function formatDistance(value) {
    if (value == null) return "Ukendt";

    return Number(value).toLocaleString("da-DK", {
        minimumFractionDigits: 1,
        maximumFractionDigits: 2
    });
}
