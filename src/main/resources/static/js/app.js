const searchForm = document.getElementById("searchForm");
const radiusInput = document.getElementById("radius");
const radiusValue = document.getElementById("radiusValue");
const resultStatus = document.getElementById("resultStatus");
const stationList = document.getElementById("stationList");

// Slideren: vis det valgte tal
radiusInput.addEventListener("input", () => {
    radiusValue.textContent = radiusInput.value;
});

// Formularen: søg i stedet for at genindlæse siden
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
        const response = await fetch(`/api/fuelfinder/stations?fuelType=${fuelType}&radius=${radius}`);
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
    if (stations.length === 0) {
        resultStatus.textContent = "Ingen stationer fundet. Prøv en større radius.";
        return;
    }

    resultStatus.textContent = `${stations.length} stationer fundet – billigste først.`;

    stations.forEach((station) => {
        const li = document.createElement("li");
        li.textContent = `${station.name} – ${station.price} kr./l – ${station.distanceKm} km`;
        stationList.appendChild(li);
    });
}