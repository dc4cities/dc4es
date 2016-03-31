#!/usr/bin/Rscript --vanilla
library(plotrix)
library(ggplot2)
require(stats)

csvfilename="../italy/data/windprediction/analysis/windactual.csv"
predictioncsvfilename="../italy/data/windprediction/analysis/windprediction.csv"

windactual<-read.table(csvfilename, header=TRUE, sep=",");
windpred<-read.table(predictioncsvfilename, header=TRUE, sep=",");

windactual$datetime <- strptime(windactual$datetime, "%Y-%m-%dT%H:%M:%S")
windpred$datetime <- strptime(windpred$datetime, "%Y-%m-%dT%H:%M:%S")

windactual$type <- "actual"
windpred$type <- "pred"
wind <- rbind(windactual, windpred)

ggplot(data=wind, aes(x=datetime, y=power, color=type)) +
  geom_line(aes(group = type)) +
  geom_point(aes(group = type)) +
  theme_bw() +
  theme(axis.text.x = element_text(angle = 45, hjust = 1)) +
  theme(legend.position="top", axis.title.x = element_blank()) 

# Generate a pdf file.
ggsave("windForecast.pdf", width=6, height=3)
