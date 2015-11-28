namespace java com.betfair.wall.brick.sportsbook.yesnoquiz

struct EligibleBetSummary {
    1: string betId
}

service cashout {
    EligibleBetSummary getEligibleBets();
}