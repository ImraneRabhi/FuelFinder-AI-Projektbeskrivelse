const searchForm = document.getElementById("searchForm");
const radiusInput = document.getElementById("radius");
const radiusValue = document.getElementById("radiusValue");
const resultStatus = document.getElementById("resultStatus");
const stationList = document.getElementById("stationList");
const aiForm = document.getElementById("aiForm");
const aiQuestion = document.getElementById("aiQuestion");
const aiAnswer = document.getElementById("aiAnswer");

let lastStations = [];

radiusInput.addEventListener("input", () => {
    radiusValue.textContent = radiusInput.value;
});


searchForm.addEventListener("submit", (event) => {
    event.preventDefault();
    searchStations();
});

async function searchStations() {
    const fuelType = document.querySelector("input[name=fuelType]:checked").value;
    const radius = radiusInput.value;

    resultStatus.textContent = "Søger...";
    stationList.innerHTML = "";

    try {
        const response = await fetch(`/api/fuelfinder/prices?fuelType=${fuelType}`);
        if (!response.ok) {
            throw new Error("Serverfejl: " + response.status);
        }
        const stations = await response.json();
        showStations(stations);
    } catch (error) {
        resultStatus.textContent = "Kunne ikke hente tankstationer. Prøv igen.";
        console.error(error);
    }
}

function showStations(stations) {
    lastStations = stations;
    if (stations.length === 0) {
        resultStatus.textContent = "Ingen stationer fundet. Prøv en større radius.";
        return;
    }

    resultStatus.textContent = `${stations.length} stationer fundet – billigste først.`;

    stations.forEach((station) => {
        const price = station.prices[0].price;

        const li = document.createElement("li");
        li.textContent = `${station.name} – ${price} kr./l – ${station.address}`;
        stationList.appendChild(li);
    });
}


aiForm.addEventListener("submit", (event) => {
    event.preventDefault();
    askAi();
});

async function askAi() {
    aiAnswer.textContent = "AI'en tænker...";


    const stationData = lastStations.slice(0, 10).map((s) =>
        `${s.name}, ${s.address}: ${s.prices[0].price} kr./l`
    ).join("\n");

    try {
        const response = await fetch("/api/fuelfinder/askai", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                question: aiQuestion.value,
                stationData: stationData
            })
        });
        if (!response.ok) {
            throw new Error("Serverfejl: " + response.status);
        }
        aiAnswer.textContent = await response.text();
    } catch (error) {
        aiAnswer.textContent = "AI'en kunne ikke svare lige nu. Prøv igen.";
        console.error(error);
    }
}