/*
 * This code is part of Rice Comp215 and is made available for your
 * use as a student in Comp215. You are specifically forbidden from
 * posting this code online in a public fashion (e.g., on a public
 * GitHub repository) or otherwise making it, or any derivative of it,
 * available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being
 * reported to the Honor Council, even after you've completed the
 * class, and will result in retroactive reductions to your grade. For
 * additional details, please see the Comp215 course syllabus.
 */


/**
 * Author: Tim Van Baak
 * Based in part on a previous version by Clayton Drazner and Matthew Kindy II
 */

$(document).ready(function () {
    "use strict";

    // We track these on our side to prevent the client from scrolling off past where the server has generated.
    // These are mutated by control UI buttons and server responses.
    var currentGeneration = 0;
    var maximumGeneration = 0;

    // This value usually remains constant despite changes to the input box, and is the actual reference number.
    // It takes on the value of the input box on a reset.
    var imageCount = 60;

    // Tracks which images have been selected.
    var selectedImages = [];

    // Stores nocache tags whenever it's safe to allow images to be cached.
    var cacheTags = [];

    // When safeNav is false, generation nav boundary checks are skipped. Currently there is no way to enable this
    // in the UI. If you want to enable it, change the value here and relaunch the client.
    var safeNav = true;

    /*
     * Logging functions that also log to the server
     */
    function ilog(s) {
        console.log(s);
        $.ajax({
            type: "POST",
            url: "/log/i/?msg=" + s
        });
    }

    function elog(s) {
        console.log(s);
        $.ajax({
            type: "POST",
            url: "/log/e/?msg=" + s
        });
    }

    /*
     * Function to validate JSON responses from the server
     */
    function validate(data, source) {
        if (!data.hasOwnProperty("response")) {
            elog("Error: \"response\" not found in return from " + source);
            return false;
        }
        if (!data.response.hasOwnProperty("numGenerations")) {
            elog("Error: \"numGenerations\" not found in " + source + " response");
            return false;
        }
        if (!data.response.hasOwnProperty("currentGeneration")) {
            elog("Error: \"currentGeneration\" not found in " + source + " response");
            return false;
        }
        if (!data.response.hasOwnProperty("numImages")) {
            elog("Error: \"numImages\" not found in " + source + " response");
            return false;
        }
        return true;
    }

    /**
     * Performs necessary initializations on page load, including initializing the control UI.
     */
    window.onload = function () {
        // Register click handlers
        $("#buttonTest1").on("click", function () {
            buttonTest(1)
        });
        $("#buttonTest2").on("click", function () {
            buttonTest(2)
        });
        $("#buttonTest3").on("click", function () {
            buttonTest(3)
        });
        $("#buttonTest4").on("click", function () {
            buttonTest(4)
        });
        $("#buttonFresh").on("click", doReset);
        $("#buttonReset").on("click", buttonReset);
        $("#buttonPrevious").on("click", buttonPrevious);
        $("#buttonNext").on("click", buttonNext);
        $("#buttonBreed").on("click", buttonBreed);

        // Enable initialization buttons
        $("#buttonTest1").prop("disabled", false);
        $("#buttonTest2").prop("disabled", false);
        $("#buttonTest3").prop("disabled", false);
        $("#buttonTest4").prop("disabled", false);
        $("#buttonFresh").prop("disabled", false);

        // Connect to the server to see if we can resume a prior run
        $.ajax({
            type: "GET",
            url: "/client-init/",
            success: function (data) {
                console.log("/client-init/ succeeded: " + data);

                // Ensure that response is well-formed
                data = JSON.parse(data);
                if (!validate(data, "/client-init/")) {
                    return;
                }
                // Ensure there are extant generations
                if (data.response.numGenerations < 1) {
                    ilog("Server reported no generations, disabling resume");
                    return;
                }
                if (data.response.numImages < 1) {
                    ilog("Server reported generations but no images, disabling resume");
                    return;
                }
                ilog("Server reported extant images, resume enabled");

                // Enable the init button and set its onclick up to resume
                $("#buttonInit").on("click", function () {
                    ilog("Client starting");
                    // Update client information
                    updateClientFromData(data);
                    // Initialize cache tags
                    for (var i = 0; i <= maximumGeneration; i++) {
                        cacheTags[i] = "?_=" + new Date().getTime();
                    }
                    // Display the received generation
                    displayGenImages(currentGeneration, imageCount);
                    // Conditionally enables the prev/next buttons
                    setGeneration(currentGeneration, maximumGeneration);
                    // Enable the rest of the control UI
                    $("#buttonReset").prop("disabled", false);
                    $("#buttonBreed").prop("disabled", false);
                });
                $("#buttonInit").prop("disabled", false);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                elog("/init/ failed (status: " + textStatus + ") (error: " + errorThrown + ")");

                // Set default values
                setGeneration(0, 0);
                setImageCount(60);

                // The start button remains disabled if /init/ fails
            }
        });
    };

    /**
     * Updates generations and image count from a JSON response according to the server API.
     * Does not check for data being well-formed - check with validate() first!
     */
    function updateClientFromData(data) {
        var res = data.response;            // Pull the response object out
        setImageCount(res.numImages);       // Set the local variable and sync the input box
        if (res.numGenerations > 0) {       // If there is at least one generation of images
            setGeneration(res.currentGeneration, res.numGenerations - 1);   // Start at the received generation
        } else {                            // If there are no generations
            setGeneration(0, 0);            // Set to default values
        }
    }

    /**
     * Updates the local generation fields. If browse mode is safe, checks if any control UI elements need to be locked.
     */
    function setGeneration(current, maximum) {
        // Cache the current selection of images in the selection cache
        selectedImages[currentGeneration] = [];
        $(".selected").each(function () {
            selectedImages[currentGeneration].push(this.imgIndex);
        });
        // Check if we're out of bounds
        if (current > maximum && safeNav) {
            elog("ERROR: setGeneration(" + current + ", " + maximum + ") called in safe nav mode");
            current = maximum;
        }
        if (current < 0 && safeNav) {
            elog("ERROR: setGeneration(" + current + ", " + maximum + ") called in safe nav mode");
            current = 0;
        }
        // Set new generation index
        currentGeneration = current;
        maximumGeneration = maximum;
        $("#buttonPrevious").prop("disabled", currentGeneration <= 0 && safeNav);
        $("#buttonNext").prop("disabled", currentGeneration >= maximumGeneration && safeNav);
        $("#generationCount").val("Generation " + currentGeneration + "/" + maximumGeneration);
    }

    /**
     * Updates both the local imageCount tracker and the numerical input box
     */
    function setImageCount(numImages) {
        imageCount = numImages;
        $("#imageCount").val(imageCount);
    }

    /**
     * Clears the picture display divs and loads imageCount images from generation genNum.
     * The resulting img objects have an imgIndex attribute recording their server-side index in their generation.
     */
    function displayGenImages(genNum, numImages) {
        ilog("Loading " + numImages + " images from generation " + genNum);

        // Clear the display area
        var cw = $("#content-wrapper");
        cw.html("");

        // Use a dummy parameter to append to image requests to prevent cached results
        var nocache = cacheTags[genNum];

        // Create all the images and add them
        for (var i = 0; i < numImages; i++) {
            // Create a new div to store the image complex in
            var imgDiv = document.createElement('div');
            imgDiv.className = "item";
            $(imgDiv).hover(function () {
                $(this).children("button").fadeTo(200, 100);
            }, function () {
                $(this).children("button").fadeTo(100, 0);
            });

            // Add the image
            var imgSrc = "/image/gen/" + genNum + "/img/" + i + "/height/201/width/201/";
            var img = document.createElement('img');
            img.src = imgSrc + nocache; // The URL to request the image from and a cache-preventing parameter
            img.imgIndex = i;           // The server-side index of the image in its generation
            img.onclick = function () {  // The selector function
                $(this).toggleClass("selected");
            };
            if ($.inArray(i, selectedImages[currentGeneration]) > -1) { // If this image's index is in the selection cache,
                img.className = "selected";                             // then select it from the get-go
            }
            imgDiv.appendChild(img);

            // Add the zoom button
            var zoomBtn = document.createElement('button');
            zoomBtn.className = "zoom-btn mui-btn mui-btn--small";
            zoomBtn.zoomSrc = "/image/gen/" + genNum + "/img/" + i + "/height/801/width/801/";
            zoomBtn.innerHTML = "&#128269;";
            zoomBtn.onclick = function () {
                ilog("Requesting zoom: " + this.zoomSrc);
                // Create the overlay
                var overlay = document.createElement('div');
                overlay.style.height = '100%';              // The overlay will take up exactly all the screen
                overlay.style.display = 'inline-block';     // In the CSS we center text, so inlines are centered
                // Insert the zoomed image ito the overlay
                var img = document.createElement('img');
                img.src = this.zoomSrc;                     // The full 800x is requested,
                img.style.height = "100%";                  // but image is downsized to the window height
                img.style.maxHeight = "800px";              // but not upsized if window height > 800
                img.onclick = function () {
                    window.open(this.src);
                };  // Clicking opens the image in full
                // Stack it all on the MUI overlay
                overlay.appendChild(img);
                mui.overlay('on', overlay);
            };
            zoomBtn.style.opacity = "0";
            imgDiv.appendChild(zoomBtn);

            // Add the genotype button
            var genoBtn = document.createElement('button');
            genoBtn.className = "geno-btn mui-btn mui-btn--small";
            genoBtn.genoSrc = "/string/gen/" + genNum + "/img/" + i + "/";
            genoBtn.innerHTML = "&#9892;";
            genoBtn.onclick = function () {
                ilog("Opening " + this.genoSrc);
                window.open(this.genoSrc);
            };
            genoBtn.style.opacity = "0";
            imgDiv.appendChild(genoBtn);

            // Append the complex to the display div
            cw.append(imgDiv);
        }
    }

    /**
     * Sends an init request to the server and initializes the breeder state to the first generation.
     */
    function doReset() {
        // Read the numerical input for image count
        var inputCount = $("#imageCount").val();
        if (inputCount !== "") {
            setImageCount(inputCount);
        }

        // Reset the control UI
        $("#buttonReset").prop("disabled", false);
        $("#buttonPrevious").prop("disabled", true);
        setGeneration(0, 0);
        $("#buttonNext").prop("disabled", true);
        $("#buttonBreed").prop("disabled", false);

        // Send a reset request with a callback to display generation 0
        $("#content-wrapper").html("");
        $.ajax({
            type: "POST",
            url: "/reset/" + imageCount + "/",
            success: function (data) {
                console.log("/reset/ succeeded: " + data);

                // Ensure the response is well-formed
                data = JSON.parse(data);
                if (!validate(data, "/reset/")) {
                    return;
                }

                // If response is well-formed, then we can use it to update the client
                updateClientFromData(data);

                // Reset cache tags
                cacheTags = [];
                cacheTags[0] = "?_=" + new Date().getTime();
                displayGenImages(currentGeneration, imageCount);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                elog("/reset/ failed (status: " + textStatus + ") (error: " + errorThrown + ")");
            }
        });
    }

    /**
     * Button click handlers
     */

    /**
     * For the Reset button. Wraps the reset function with a confirmation box.
     */
    function buttonReset() {
        if (confirm("Reset to new generation 0?")) {    // Confirm reset with user
            doReset();
        }
    }

    /**
     * For the Previous button. Moves the breeder back a generation and displays the generation's images.
     */
    function buttonPrevious() {
        setImageCount(imageCount);                                  // Assert image count to the input box
        setGeneration(currentGeneration - 1, maximumGeneration);    // Move backward
        displayGenImages(currentGeneration, imageCount);            // Display new generation
    }

    /**
     * For the Next button. Moves the breeder forward a generation and displays the generation's images.
     */
    function buttonNext() {
        setImageCount(imageCount);                                  // Assert image count to the input box
        setGeneration(currentGeneration + 1, maximumGeneration);    // Move forward
        displayGenImages(currentGeneration, imageCount);            // Display new generation
    }

    /**
     * For the Breed button. If #selection contains at least two images, sends a breed request of the selected images
     * and displays the new generation.
     */
    function buttonBreed() {
        var selected = $(".selected");
        var url = "/breed/oldgen/";

        if (selected.length > 1) {  // If there are enough selected images
            // Create a normal breed URL
            url = url + currentGeneration + "/img/";
            selected.each(function () {
                url = url + this.imgIndex + "/";
            });
            console.log("Sending breed request: " + url);
        } else if (selected.length === 0 && currentGeneration > 0 &&     // If there's no selection, but a previous generation
            currentGeneration === maximumGeneration &&               // and we're on the last generation
            selectedImages[currentGeneration - 1].length > 1) {     // and there's a selection on the previous generation
            // Create a breed URL from the previous selection
            url = url + (currentGeneration - 1) + "/img/";
            url = url + selectedImages[currentGeneration - 1].join("/") + "/";
            console.log("Sending rebreed request: " + url);
        } else {    // Otherwise, the current selection is invalid
            alert("Not enough pictures for breeding: need at least two");
            console.log("Breeding aborted");
            return;
        }

        // Send the POST request with a callback to display the new generation
        $.ajax({
            type: "POST",
            url: url,
            success: function (data) {
                console.log("/breed/ succeeded: " + data);

                // Ensure the response is well-formed
                data = JSON.parse(data);
                if (!validate(data, "/breed/")) {
                    return;
                }

                // Clear selection and nocache information for discarded generations
                for (var i = currentGeneration + 1; i <= maximumGeneration; i++) {
                    selectedImages[i] = [];
                    // These will probably just be overwritten, but just in case, changing them makes sure
                    cacheTags[i] = "?_=" + new Date().getTime();
                }

                updateClientFromData(data);

                // Update nocache for this generation in case of rebreeding
                cacheTags[currentGeneration] = "?_=" + new Date().getTime();

                displayGenImages(currentGeneration, imageCount);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                elog("/breed/ failed (status: " + textStatus + ") (error: " + errorThrown + ")");
            }
        });
    }

    // For the Test button. Sends a test generation request to the server.
    function buttonTest(number) {
        $.ajax({
            type: "POST",
            url: "/test/" + number,
            success: function (data) {
                console.log("/test/ succeeded: " + data);

                // Ensure that response is well-formed
                data = JSON.parse(data);
                if (!validate(data, "/test/")) {
                    return;
                }
                // The test generation should be one generation
                if (data.response.numGenerations < 1) {
                    elog("Test generation has no generations in it");
                    return;
                }
                if (data.response.numImages < 1) {
                    elog("Test generation has no images in it");
                    return;
                }
                updateClientFromData(data);

                // Initialize cache tags for all received generations
                for (var i = 0; i <= maximumGeneration; i++) {
                    cacheTags[i] = "?_=" + new Date().getTime();
                }

                // Display the test generation
                ilog("Displaying test generation");
                displayGenImages(currentGeneration, imageCount);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                elog("/test/ failed (status: " + textStatus + ") (error: " + errorThrown + ")");
            }
        });
    }
});
