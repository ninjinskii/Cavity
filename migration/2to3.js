const oldBottles = require("./bouteille_table.json");
const oldWines = require("./vin_table.json");

let lastHistoryEntryId = 1;

const counties = makeCounties();
const wines = makeWines(counties);
const reviews = makeReviews();
const fReviews = makeFReviews(reviews);
const grapes = makeGrapes();
const bottles = makeBottles();

run(counties, wines, reviews, fReviews, grapes, bottles);

function makeCounties() {
  const counties = [];
  let index = 1;

  for (const wine of oldWines) {
    if (counties.find((c) => c.name === wine.region) === undefined) {
      counties.push({
        id: index,
        name: wine.region,
        pref_order: index++,
      });
    }
  }

  return counties;
}

function makeWines(counties) {
  const wines = [];

  for (const [index, wine] of oldWines.entries()) {
    const color = getWineColor(wine);
    const isOrganic = getIsOrganic(wine);
    const image = getImage(wine);
    const county = getCountyForWine(counties, wine);

    wines.push({
      id: wine.id,
      name: wine.name,
      naming: wine.naming,
      color,
      cuvee: "",
      is_organic: isOrganic ? 1 : 0,
      image,
      county_id: county.id,
    });
  }

  return wines;
}

function makeReviews() {
  const reviews = [
    { contest_name: "Parker", type: 2 },
    { contest_name: "Figaro", type: 1 },
    { contest_name: "Hachette", type: 3 },
    { contest_name: "James Suckling", type: 2 },
    { contest_name: "Bordeaux", type: 0 },
    { contest_name: "Féminalise", type: 0 },
    { contest_name: "Paris", type: 0 },
    { contest_name: "Yves Beck", type: 2 },
    { contest_name: "Vignerons indépendants", type: 0 },
    { contest_name: "Revue des vins de France", type: 1 },
    { contest_name: "Bicchieri Gambero Rosso", type: 3 },
    { contest_name: "Challenge International", type: 0 },
    { contest_name: "Bettane & Dessauve", type: 2 },
    { contest_name: "Jeb Dunnuck", type: 2 },
    { contest_name: "Guide des meilleurs vins de France", type: 1 },
    { contest_name: "Decanter", type: 2 },
    { contest_name: "Wine enthusiast", type: 2 },
    { contest_name: "Lyon", type: 0 },
  ];

  reviews.forEach((r, index) => (r.id = index + 1));

  return reviews;
}

function makeFReviews(reviews) {
  const fReviews = [];

  for (const bottle of flattenOldBottles()) {
    if (bottle.commentaireNote.trim() === "") {
      continue;
    }

    const comment = bottle.commentaireNote.trim().toLowerCase().split("/")[0];

    for (const r of reviews) {
      const contest = r.contest_name.toLowerCase();

      if (comment.includes(contest)) {
        // MEDALS
        if (r.type === 0 && bottle.distinction.includes("#")) {
          let value = null;

          if (bottle.distinction.split("#")[1] === "C0C0C0") {
            value = 1;
          }

          if (bottle.distinction.split("#")[1] === "FFD700") {
            value = 2;
          }

          if (value) {
            fReviews.push({ bottle_id: bottle.id, review_id: r.id, value });
          }
        }

        // RATE & STARS
        if (r.type === 1 || r.type === 2 || r.type === 3) {
          fReviews.push({
            bottle_id: bottle.id,
            review_id: r.id,
            value: bottle.distinction.substring(1),
          });
        }
      }
    }

    // Secondary constests. Expected to be located right after a "/" with the value as the first two chars
    if (comment.includes("/")) {
      const secondaryContest = comment.split("/")[1].trim().toLowerCase();
      const value = parseInt(secondaryContest.substring(0, 2));
      const contest = reviews.find((r) =>
        secondaryContest.includes(r.contest_name.toLowerCase())
      );

      fReviews.push({ bottle_id: bottle.id, review_id: contest.id, value });
    }
  }

  return fReviews;
}

function makeGrapes() {
  const grapes = [{ name: "Merlot" }, { name: "Cabernet sauvignon" }];

  grapes.forEach((g, index) => (g.id = index + 1));

  return grapes;
}

function makeBottles() {
  const oldBottles = flattenOldBottles();
  const bottles = [];
  const historyEntries = [];

  for (const bottle of oldBottles) {
    const newBottle = {
      id: bottle.id,
      wine_id: bottle.vin_id,
      vintage: bottle.millesime,
      apogee: bottle.apogee,
      is_favorite: bottle.fav,
      price: bottle.prixAchat,
      currency: "€",
      buy_location: bottle.lieuxAchat,
      buy_date: getBottleBuyDate(bottle),
      taste_comment: bottle.commentaire,
      pdf_path: bottle.pdf_path,
      consumed: 0,
      other_info: "",
      tasting_id: null,
    };

    historyEntries.push(...getGenericHistoryEntries(bottle));

    if (bottle.noHistory) {
      newBottle.consumed = 1;
    }

    bottles.push(newBottle);
  }

  return { bottles, historyEntries };
}

function getWineColor(wine) {
  let color = null;

  switch (wine.color) {
    case "#A60000":
      color = "RED";
      break;
    case "#FFA727":
      color = "SWEET";
      break;
    case "#fff176":
      color = "WHITE";
      break;
    default:
      color = "ROSE";
  }

  return color;
}

function getIsOrganic(wine) {
  const bottles = getBottlesForWine(wine);

  return bottles.some(
    (b) =>
      b.commentaireNote.toLowerCase().includes("bio") &&
      !b.commentaireNote.toLowerCase().includes("nebiolo")
  );
}

function getImage(wine) {
  const bottles = getBottlesForWine(wine);
  const imagedBottle = bottles.find((b) => b.imgPath);

  return imagedBottle ? imagedBottle.imgPath : "";
}

function getCountyForWine(counties, wine) {
  return counties.find((c) => wine.region === c.name);
}

function getBottlesForWine(wine) {
  return oldBottles.filter((b) => b.vin_id === wine.id);
}

function flattenOldBottles() {
  const bottles = [];

  for (bottle of oldBottles) {
    if (bottle.nombre === 0) {
      bottle.noHistory = true;
      bottles.push(bottle);
    }

    if (bottle.nombre === 1) {
      bottles.push(bottle);
    }

    if (bottle.nombre > 1) {
      let index = 0;

      while (index < bottle.nombre - 1) {
        bottles.push(bottle);
        index++;
      }
    }
  }

  bottles.forEach((b, index) => (b.id = index + 1));

  return bottles;
}

function getGenericHistoryEntries(bottle) {
  const entry = {
    id: lastHistoryEntryId++,
    date: 1577836800001, // 1 january 2020 00h00
    bottle_id: bottle.id,
    tasting_id: null,
    comment: "La date exacte n'est pas connue.",
    type: 1, // replenishment
    favorite: 0,
  };

  const result = [entry];

  if (bottle.noHistory) {
    result.push({
      ...entry,
      id: lastHistoryEntryId++,
      date: 1577836800002,
      type: 0, // consumption
    });
  }

  return result;
}

function getBottleBuyDate(bottle) {
  const [day, month, year] = bottle.dateAchat.split("/");
  return Date.parse(`20${year}-${month}-${day}`);
}

function run(counties, wines, reviews, fReviews, grapes, bottles) {
  const fs = require("fs");
  const content = {
    counties,
    wines,
    reviews,
    fReviews,
    grapes,
    bottles: bottles.bottles,
    historyEntries: bottles.historyEntries,
  };

  try {
    const data = fs.writeFileSync("data.json", JSON.stringify(content));
  } catch (error) {
    console.log(error);
  }
}
