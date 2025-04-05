alias gw="./gradlew"

function gw-run() {
  if [ -z "$1" ]; then
    echo "Usage: gw-run <classpath>"
    return 1
  fi

  gw --warning-mode=none runMain -PmainClass="$1"
}