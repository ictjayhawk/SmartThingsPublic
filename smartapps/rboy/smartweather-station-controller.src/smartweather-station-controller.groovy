/* **DISCLAIMER**
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * Without limitation of the foregoing, Contributors/Regents expressly does not warrant that:
 * 1. the software will meet your requirements or expectations;
 * 2. the software or the software content will be free of bugs, errors, viruses or other defects;
 * 3. any results, output, or data provided through or generated by the software will be accurate, up-to-date, complete or reliable;
 * 4. the software will be compatible with third party software;
 * 5. any errors in the software will be corrected.
 * The user assumes all responsibility for selecting the software and for the results obtained from the use of the software. The user shall bear the entire risk as to the quality and the performance of the software.
 */ 
 
 /**
 *  Weather Station Controller
 *
 *  Copyright 2014 SmartThings
 *	Bugfixed by RBoy
 *  Version 1.5
 *  2016-2-12 - Changed scheduling API's (hopefully more resilient), added an option for users to specify update interval
 *  2016-1-20 - Kick start timers on sunrise and sunset also
 *  2015-10-4 - Kick start timers on each mode change to prevent them from dying
 *  2015-7-12 - Simplified app, udpates every 5 minutes now (hopefully more reliable)
 *  2015-7-17 - Improved reliability when mode changes
 *	2015-6-6 - Bugfix for timers not scheduling, keep only one timer
 *			 Added support to update multiple devices
 *			 Added support for frequency of updates            
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

definition(
    name: "SmartWeather Station Controller",
    namespace: "rboy",
    author: "RBoy",
    description: "Updates SmartWeather Station Tile devices every hour. This contains a bug fix for the updates stops when user select custom modes and it updates every 5 minutes",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome@2x.png"
)

preferences {
    section ("Weather Devices") {
        input name: "weatherDevices", type: "device.smartweatherStationTile", title: "Select Weather Device(s)", description: "Select the Weather Tiles to update", required: true, multiple: true
        input name: "updateInterval", type: "number", title: "Enter update frequency (minutes)", description: "How often do you want to update the weather information", required: true, defaultValue: 5
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    if (updateInterval <= 0) {
    	log.error "Invalid weather update interval $updateInterval minutes. Minimum update interval should be 1 minute"
        sendNotification "Invalid weather update interval $updateInterval minutes. Minimum update interval should be 1 minute"
    }

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    subscribe(location, modeChangeHandler)
    subscribe(location, "sunset", modeChangeHandler)
    subscribe(location, "sunrise", modeChangeHandler)
    scheduledEvent()
}

def modeChangeHandler(evt) {
    log.debug "Reinitializing refresh timers on mode change notification, new mode $evt.value"
    scheduledEvent()
}

def scheduledEvent() {
    log.trace "Refresh weather, update frequency $updateInterval minutes"
    runIn(updateInterval*60, scheduledEvent)
    weatherDevices.refresh()
}