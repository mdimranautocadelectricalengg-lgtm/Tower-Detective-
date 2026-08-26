import {
    registerPlugin
} from '@capacitor/core';

import {
    Geolocation
} from '@capacitor/geolocation';


const TowerDetector =
    registerPlugin('TowerDetector');


let scannerTimer = null;

let gpsWatchId = null;

let lastResult = null;


/* --------------------------------
   ELEMENTS
-------------------------------- */

const status =
    document.getElementById("status");

const network =
    document.getElementById("network");

const operator =
    document.getElementById("operator");

const latitude =
    document.getElementById("latitude");

const longitude =
    document.getElementById("longitude");

const accuracy =
    document.getElementById("accuracy");

const altitude =
    document.getElementById("altitude");

const cells =
    document.getElementById("cells");

const servingCell =
    document.getElementById("servingCell");

const map =
    document.getElementById("map");

const aiResult =
    document.getElementById("aiResult");


/* --------------------------------
   START
-------------------------------- */

document
    .getElementById("startButton")
    .addEventListener(
        "click",
        startScanner
    );


/* --------------------------------
   STOP
-------------------------------- */

document
    .getElementById("stopButton")
    .addEventListener(
        "click",
        stopScanner
    );


/* --------------------------------
   AI BUTTON
-------------------------------- */

document
    .getElementById("analyzeButton")
    .addEventListener(
        "click",
        analyzeCurrentData
    );


/* --------------------------------
   START SCANNER
-------------------------------- */

async function startScanner() {

    try {

        status.innerText =
            "Requesting permissions...";


        /*
         * GPS permission
         */

        await Geolocation
            .requestPermissions();


        /*
         * Cellular permission
         */

        await TowerDetector
            .requestPermissions();


        /*
         * Start GPS
         */

        startGPS();


        /*
         * First cellular read
         */

        await readCells();


        /*
         * Repeat cellular reads
         */

        scannerTimer =
            setInterval(
                readCells,
                5000
            );


        status.innerText =
            "Live monitoring";


    } catch (error) {

        console.error(error);

        status.innerText =
            "Permission error";

        alert(
            "Permission error:\n\n"
            + error
        );
    }
}


/* --------------------------------
   GPS
-------------------------------- */

async function startGPS() {

    try {

        if (gpsWatchId !== null) {

            return;

        }


        gpsWatchId =
            await Geolocation.watchPosition(
                {
                    enableHighAccuracy: true,

                    timeout: 10000,

                    maximumAge: 2000
                },

                (position, error) => {

                    if (error) {

                        console.error(
                            "GPS error",
                            error
                        );

                        return;
                    }


                    if (!position) {

                        return;
                    }


                    const c =
                        position.coords;


                    latitude.innerText =
                        Number(
                            c.latitude
                        ).toFixed(6);


                    longitude.innerText =
                        Number(
                            c.longitude
                        ).toFixed(6);


                    accuracy.innerText =
                        Math.round(
                            c.accuracy
                        ) + " m";


                    altitude.innerText =
                        c.altitude == null
                            ? "Unavailable"
                            : Math.round(
                                c.altitude
                              ) + " m";


                    map.innerHTML =
                        "📍<br><br>"
                        +
                        Number(
                            c.latitude
                        ).toFixed(6)
                        +
                        ", "
                        +
                        Number(
                            c.longitude
                        ).toFixed(6)
                        +
                        "<br><br>"
                        +
                        "Accuracy: "
                        +
                        Math.round(
                            c.accuracy
                        )
                        +
                        " m";
                }
            );

    } catch (error) {

        console.error(
            "GPS start failed",
            error
        );
    }
}


/* --------------------------------
   CELL INFORMATION
-------------------------------- */

async function readCells() {

    try {

        const result =
            await TowerDetector
                .getCellInfo();


        console.log(
            "Cell result:",
            result
        );


        lastResult =
            result;


        renderNetwork(
            result
        );


        renderCells(
            result
        );


        status.innerText =
            "Live monitoring";


    } catch (error) {

        console.error(
            "Cell error:",
            error
        );


        status.innerText =
            "Cell information unavailable";


        cells.innerHTML =
            `
            <p class="small">
                Android did not return
                cellular information.
                <br><br>
                Make sure Location is ON
                and required permissions
                are allowed.
            </p>
            `;
    }
}


/* --------------------------------
   NETWORK
-------------------------------- */

function renderNetwork(result) {

    network.innerText =
        result.networkType
        || "--";


    operator.innerText =
        result.operator
        || "--";


    const list =
        result.cells || [];


    const serving =
        list.find(
            cell =>
                cell.registered === true
        );


    if (!serving) {

        servingCell.innerHTML =
            `
            <p class="small">
                No serving cell reported.
            </p>
            `;

        return;
    }


    servingCell.innerHTML =
        `
        <div class="cell">

            <h3>
                🟢
                ${escapeHTML(
                    serving.type || "Cell"
                )}
                — Serving
            </h3>

            <div class="grid">

                ${field(
                    "Cell ID",
                    serving.cellId
                )}

                ${field(
                    "Signal",
                    serving.signalDbm
                    + " dBm"
                )}

                ${field(
                    "MCC",
                    serving.mcc
                )}

                ${field(
                    "MNC",
                    serving.mnc
                )}

                ${field(
                    "TAC/LAC",
                    serving.tac
                )}

                ${field(
                    "PCI",
                    serving.pci
                )}

            </div>

        </div>
        `;
}


/* --------------------------------
   CELLS
-------------------------------- */

function renderCells(result) {

    const list =
        result.cells || [];


    if (!list.length) {

        cells.innerHTML =
            `
            <p class="small">
                No cell information returned
                by Android.
            </p>
            `;

        return;
    }


    cells.innerHTML = "";


    list.forEach(
        (cell) => {

            const signal =
                Number(
                    cell.signalDbm
                    ?? -120
                );


            const percentage =
                Math.max(
                    0,
                    Math.min(
                        100,
                        (
                            (signal + 120)
                            / 70
                        ) * 100
                    )
                );


            const div =
                document.createElement(
                    "div"
                );


            div.className =
                "cell";


            div.innerHTML =
                `
                <h3>

                    ${
                        cell.registered
                            ? "🟢 Serving"
                            : "⚪ Neighbor"
                    }

                    ${escapeHTML(
                        cell.type
                        || "Cell"
                    )}

                </h3>


                <div class="grid">

                    ${field(
                        "Cell ID",
                        cell.cellId
                    )}

                    ${field(
                        "Signal",
                        cell.signalDbm
                        + " dBm"
                    )}

                    ${field(
                        "MCC",
                        cell.mcc
                    )}

                    ${field(
                        "MNC",
                        cell.mnc
                    )}

                    ${field(
                        "TAC/LAC",
                        cell.tac
                    )}

                    ${field(
                        "PCI",
                        cell.pci
                    )}

                </div>


                <div class="signal">

                    <div
                        class="signalBar"
                        style="
                            width:
                            ${percentage}%
                        "
                    >
                    </div>

                </div>
                `;


            cells.appendChild(
                div
            );
        }
    );
}


/* --------------------------------
   FIELD
-------------------------------- */

function field(
    label,
    value
) {

    return `
        <div class="item">

            <span class="label">
                ${label}
            </span>

            <strong>
                ${
                    value == null
                        ? "--"
                        : escapeHTML(
                            String(value)
                          )
                }
            </strong>

        </div>
    `;
}


/* --------------------------------
   AI
-------------------------------- */

async function analyzeCurrentData() {

    if (!lastResult) {

        aiResult.innerText =
            "Start the scanner first.";

        return;
    }


    const serving =
        (lastResult.cells || [])
            .find(
                cell =>
                    cell.registered
            );


    if (!serving) {

        aiResult.innerText =
            "No serving cell data available.";

        return;
    }


    const signal =
        Number(
            serving.signalDbm
        );


    let interpretation =
        "Unknown";


    if (signal >= -80) {

        interpretation =
            "Generally strong";

    } else if (signal >= -95) {

        interpretation =
            "Moderate";

    } else if (signal >= -110) {

        interpretation =
            "Weak";

    } else {

        interpretation =
            "Very weak";
    }


    aiResult.innerHTML =
        `
        <strong>
            Current network analysis
        </strong>

        <br><br>

        Technology:
        ${escapeHTML(
            serving.type || "Unknown"
        )}

        <br>

        Cell ID:
        ${escapeHTML(
            String(
                serving.cellId ?? "--"
            )
        )}

        <br>

        Signal:
        ${escapeHTML(
            String(
                serving.signalDbm
                ?? "--"
            )
        )}
        dBm

        <br><br>

        Signal interpretation:
        ${interpretation}

        <br><br>

        This is a local interpretation
        of the measured Android data.
        It does not identify the physical
        tower location.
        `;
}


/* --------------------------------
   STOP
-------------------------------- */

function stopScanner() {

    if (
        scannerTimer !== null
    ) {

        clearInterval(
            scannerTimer
        );

        scannerTimer =
            null;
    }


    if (
        gpsWatchId !== null
    ) {

        Geolocation
            .clearWatch({
                id: gpsWatchId
            });

        gpsWatchId =
            null;
    }


    status.innerText =
        "Scanner stopped";
}


/* --------------------------------
   HTML SAFETY
-------------------------------- */

function escapeHTML(value) {

    return value
        .replace(
            /&/g,
            "&amp;"
        )
        .replace(
            /</g,
            "&lt;"
        )
        .replace(
            />/g,
            "&gt;"
        )
        .replace(
            /"/g,
            "&quot;"
        )
        .replace(
            /'/g,
            "&#039;"
        );
}
