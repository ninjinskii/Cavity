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
        prefOrder: index++,
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
      name: wine.nom.trim(),
      naming: wine.appellation.trim(),
      color,
      cuvee: "",
      isOrganic: isOrganic ? 1 : 0,
      imgPath: image.trim(),
      countyId: county.id,
    });
  }

  return wines;
}

function makeReviews() {
  const reviews = [
    { contestName: "Parker", type: 2 },
    { contestName: "Figaro", type: 1 },
    { contestName: "Hachette", type: 3 },
    { contestName: "James Suckling", type: 2 },
    { contestName: "Bordeaux", type: 0 },
    { contestName: "Féminalise", type: 0 },
    { contestName: "Paris", type: 0 },
    { contestName: "Yves Beck", type: 2 },
    { contestName: "Vignerons indépendants", type: 0 },
    { contestName: "Revue des vins de France", type: 1 },
    { contestName: "Bicchieri Gambero Rosso", type: 3 },
    { contestName: "Challenge International", type: 0 },
    { contestName: "Bettane & Dessauve", type: 2 },
    { contestName: "Jeb Dunnuck", type: 2 },
    { contestName: "Guide des meilleurs vins de France", type: 1 },
    { contestName: "Decanter", type: 2 },
    { contestName: "Wine enthusiast", type: 2 },
    { contestName: "Lyon", type: 0 },
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
      const contest = r.contestName.toLowerCase();

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
            fReviews.push({ bottleId: bottle.id, reviewId: r.id, value });
          }
        }

        // RATE & STARS
        if (r.type === 1 || r.type === 2 || r.type === 3) {
          fReviews.push({
            bottleId: bottle.id,
            reviewId: r.id,
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

      fReviews.push({ bottleId: bottle.id, reviewId: contest.id, value });
    }
  }

  // sanity primary key check
  fReviews.forEach((checkedFReview) => {
    if (
      fReviews.filter(
        (fReview) =>
          fReview.bottleId === checkedFReview.bottleId &&
          fReview.reviewId === checkedFReview.reviewId
      ).length > 1
    ) {
      throw Error(
        `Primary key constraint failed with review ${checkedFReview.reviewId} and bottle ${checkedFReview.bottleId}`
      );
    }
  });

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
      wineId: bottle.vin_id,
      vintage: bottle.millesime,
      apogee: bottle.apogee,
      isFavorite: bottle.fav,
      count: 0,
      price: parseFloat(bottle.prixAchat + ".0"),
      currency: "€",
      buyLocation: bottle.lieuxAchat.trim(),
      buyDate: getBottleBuyDate(bottle),
      tastingTasteComment: "",
      pdfPath: bottle.pdf_path || "",
      consumed: bottle.consumed,
      otherInfo: "",
      tastingId: null,
    };

    historyEntries.push(...getGenericHistoryEntries(bottle, newBottle.buyDate));

    bottles.push(newBottle);
  }

  return { bottles, historyEntries };
}

function getWineColor(wine) {
  let color = null;

  switch (wine.couleur) {
    case "#A60000":
      color = "RED";
      break;
    case "#FFB400":
    case "#FFA727":
      color = "SWEET";
      break;
    case "#FFDE40":
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
  let id = 1;

  for (const bottle of oldBottles) {
    bottle.id = id++;
    bottle.consumed = 0;

    if (parseInt(bottle.nombre) === 0) {
      bottle.consumed = 1;
      console.log("bottle: consumed");
      bottles.push(bottle);
    }

    if (parseInt(bottle.nombre) === 1) {
      bottles.push(bottle);
    }

    if (parseInt(bottle.nombre) > 1) {
      let index = 0;

      while (index < bottle.nombre) {
        const copy = { ...bottle, id };
        id++;
        bottles.push(copy);
        index++;
      }
    }
  }

  bottles.forEach((b, index) => (b.id = index + 1));

  return bottles;
}

function getGenericHistoryEntries(bottle, date) {
  const entry = {
    id: lastHistoryEntryId++,
    date,
    bottleId: bottle.id,
    tastingId: null,
    comment: "La date exacte n'est pas connue.",
    type: 1, // replenishment
    favorite: 0,
  };

  const result = [entry];

  if (bottle.consumed === 1) {
    result.push({
      ...entry,
      id: lastHistoryEntryId++,
      date: date + 1,
      type: 0, // consumption
      comment: bottle.commentaire.trim(),
    });
  }

  return result;
}

function getBottleBuyDate(bottle) {
  if (bottle.dateAchat.length > 0) {
    const [day, month, year] = bottle.dateAchat.split("/");
    return Date.parse(`20${year}-${month}-${day}`);
  } else {
    return 1577836800001; // 1 january 2020 00h00
  }
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
    const data = fs.writeFileSync("db.json", JSON.stringify(content));
  } catch (error) {
    console.log(error);
  }
}
