#!/usr/bin/Rscript --vanilla
library(plotrix)
require(stats)
csvfilename="/home/msheikhalishahi/Dropbox/Dc4Cities-data/energyProfiles/Data2013/gridProfile.csv";
rdafilename="/home/msheikhalishahi/Dropbox/Dc4Cities-data/energyProfiles/2013/gridProfile2013.rda";

d<-read.table(csvfilename, header=TRUE, sep=",");
month1s3<-d[d$month=='1'| d$month=='2'| d$month=='3', c(2,3,4)];
d4<-d[d$month=='4'| d$month=='5'| d$month=='6', c(2,3,4)];
month7s9<-d[d$month=='7'| d$month=='8'| d$month=='9',c(2,3,4)];
month10s12<-d[d$month=='10'| d$month=='11'| d$month=='12',c(2,3,4)];
month1s12<-d[,c(2,3,4)];
#d1<-data.frame(d1);

#d4<-d1[d1$month=='4'| d1$month=='5'| d1$month=='6',c(1,2,3)];
#month1s3<-d1[d1$month=='1'| d1$month=='2'| d1$month=='3',c(1,2,3)];
pdf("RenP.pdf");


plot(month1s12$hour, month1s12$renp, xlab = 'Hour', ylab = 'REN%', type="p", ylim=c(0,100), xlim=c(0,23), cex=0.60, pch=c(1:12)[month1s12$month], main="Renewable Electricity Percentage, Italy, 2013");
legend("topright", c("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"), pch=c(1:12));

plot(month1s3$hour, month1s3$renp, xlab = 'Hour', ylab = 'REN%', type="p", ylim=c(0,70), xlim=c(0,23), pch=c(1:3)[month1s3$month], col="red", main="Renewable Electricity Percentage, Italy, 2013");
legend("topright", c("January", "February", "March"), pch=c(1:3));

plot(d4$hour, d4$renp, xlab = 'Hour', ylab = 'REN%', type="p", xlim=c(0,23), ylim=c(0,70), pch=c(4:6)[d4$month-3], col="blue", main="Renewable Electricity Percentage, Italy, 2013");
legend("topright", c("April", "May", "June"), pch=c(4:6));

plot(month7s9$hour, month7s9$renp, xlab = 'Hour', ylab = 'REN%', type="p", xlim=c(0,23), ylim=c(0,70), pch=c(7:9)[month7s9$month-6], col="gray", main="Renewable Electricity Percentage, Italy, 2013 July");
legend("topright", c("July", "August", "September"), pch=c(7:9));

plot(month10s12$hour, month10s12$renp, xlab = 'Hour', ylab = 'REN%', type="p", xlim=c(0,23), ylim=c(0,70), pch=c(10:12)[month10s12$month-9], col="brown", main="Renewable Electricity Percentage, Italy, 2013 July");
legend("topright", c("October", "November", "December"), pch=c(10:12));

quit();

#plot(d4[,2], d4[,3], xlab = 'Hour', ylab = 'REN%', type="p", ylim=c(0,70), xlim=c(0,23), pch=c(1:3)[d4$month], main="Renewable Electricity Percentage, Italy, 2013");
#plot(d4$hour, d4$renp, xlab = 'Hour', ylab = 'REN%', type="p", ylim=c(0,70), xlim=c(0,23), pch=c(1:3)[d4$month], col="red", main="Renewable Electricity Percentage, Italy, 2013");
#legend(x,y,levels(variablename$category),pch=c(1:5)) #matches levels of category column (in alphabetical order) to symbols 1 through 5
#pdf("RenP7.pdf");
#legend(15, 50, c("January", "February", "March"), pch=c(1:3))
#legend("bottomright",c("name1","name2")) #adds legend in the bottom right corner. Also "bottom", "bottomleft", "left", "topleft", "top", "", "right" and "center".
quit();

#plot(xy.coords(jan), xlab = 'Hour', ylab = 'REN%', type="b", ylim=c(0,100), xlim=c(0,23), lty=1, main="Renewable Electricity Generation, Italy, 2013", pch=20);
#abline(h=jan$2) #adds horizontal line at specified y-coordinate
barplot(t(exp_metrics),width=c(0.6,0.6,0.5),beside=T,axes=F, col=colors)
axis(1,at=seq(1.25,11.50,by=1.70),labels=exp,las=0, cex.axis=0.8, tck=0)
#waittime, slowdown
abline(h=seq(0,maxHeight,by=0.5),col="blue",lty=2)
axis(2,at=seq(0,maxHeight,by=.2),labels=seq(0,maxHeight,by=.2),cex.axis=.8)
legend("top", legend=c("Waittime(MCBP/BKFL)","Slowdown(MCBP/BKFL)"), col=colors,cex=0.8,pt.cex=1.5, pch=c(15,15))
title(main=paste("Average scheduling performance metrics"),cex.main=1)
mtext("Workload traces/experiments", side=1, line=2, cex=1)
mtext("Normalized waittime, slowdown metrics", side=2, line=2.2, cex=1)

d1fcfs<-d1fcfs[,c(2,3,4)]
d1mcbp<-d1mcbp[,c(2,3,4)]

attach(d1fcfs)
aggdatafcfs<-aggregate(d1fcfs[,c(2,3)], by=list(tracefile), FUN=mean, na.rm=TRUE, na.omit=TRUE)
detach(d1fcfs)

attach(d1mcbp)
aggdatamcbp<-aggregate(d1mcbp[,c(2,3)], by=list(tracefile), FUN=mean, na.rm=TRUE)
detach(d1mcbp)

d1fcfs<-aggdatafcfs[,c(2,3)]
d1mcbp<-aggdatamcbp[,c(2,3)]
metricsw<-cbind(d1mcbp[,1]/d1fcfs[,1])
metricss<-cbind(d1mcbp[,2]/d1fcfs[,2])
#metricsw<-apply(metricsw, 2, function(x) x/3600)
#metricss<-apply(metricss, 2, function(x) x/10)
metrics<-cbind(metricsw, metricss)
experiment<-c()
for(ex in aggdatafcfs[,1]) {
	experiment<-rbind(experiment, substr(ex, nchar(ex) - 6, nchar(ex) - 4))
}

colors<-c('darkorange','firebrick')
#colors<-c('darkorange','darkgoldenrod1','firebrick','firebrick1')
#I should constraints this variable in case there are more experiments that we can extend to 5 we do that otherwise we limit to the remaining number of experiments
exp_per_graphic=7
it=seq(1,length(experiment),by=exp_per_graphic)
last_exp=length(experiment)
print("last_exp:")
print(last_exp)
print("metrics: ")
print(metrics)
for(i in it) {
	print(i)
	to_exp=min(i+exp_per_graphic-1, last_exp)
	exp<-experiment[c(seq(i,to_exp))]
	print("exp")
	print(exp)
	exp_metrics<-metrics[c(seq(i, to_exp)),]
	maxWidth=to_exp-i+1
	if(maxWidth==1)
		maxWidth = 2 * maxWidth
	print("maxWidth")
	print(maxWidth)
	maxHeight=max(exp_metrics)
	if(maxHeight<1)
		maxHeight=1

	pdf(paste("Mean-graph-metrics-",i, ".pdf"))
	barplot(t(exp_metrics),width=c(0.6,0.6,0.5),beside=T,axes=F, col=colors)
	axis(1,at=seq(1.25,11.50,by=1.70),labels=exp,las=0, cex.axis=0.8, tck=0)
	#waittime, slowdown
	abline(h=seq(0,maxHeight,by=0.5),col="blue",lty=2)
	box()
	axis(2,at=seq(0,maxHeight,by=.2),labels=seq(0,maxHeight,by=.2),cex.axis=.8)
	legend("top", legend=c("Waittime(MCBP/BKFL)","Slowdown(MCBP/BKFL)"), col=colors,cex=0.8,pt.cex=1.5, pch=c(15,15))
	title(main=paste("Average scheduling performance metrics"),cex.main=1)
	mtext("Workload traces/experiments", side=1, line=2, cex=1)
	mtext("Normalized waittime, slowdown metrics", side=2, line=2.2, cex=1)
}

warnings()
