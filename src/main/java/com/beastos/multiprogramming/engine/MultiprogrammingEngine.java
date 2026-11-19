package com.beastos.multiprogramming.engine;

import com.beastos.multiprogramming.model.*;
import java.util.*;

public class MultiprogrammingEngine {

    private static final double EPS = 1e-6;

    public MultiprogrammingOutput simulate(List<ProgramProcess> requests) {
        if (requests == null || requests.isEmpty()) {
            return new MultiprogrammingOutput(0, 0, 0, 0, 0, 0, 0, List.of(), List.of());
        }

        List<ProgramProcess> pending = new ArrayList<>(requests);
        pending.sort(Comparator.comparingDouble(ProgramProcess::getArrivalTime));

        List<ProgramProcess> active = new ArrayList<>();
        Map<String, Double> left = new LinkedHashMap<>();
        Map<String, Double> finishMap = new HashMap<>();
        List<String[]> rows = new ArrayList<>();

        double startTime = pending.get(0).getArrivalTime();
        double time = startTime;
        double activeCpuTime = 0.0;
        int maxNj = 0;

        // Initial arrival(s) at startTime
        List<String> initEvents = new ArrayList<>();
        while (!pending.isEmpty() && Math.abs(pending.get(0).getArrivalTime() - time) < EPS) {
            ProgramProcess p = pending.remove(0);
            active.add(p);
            left.put(p.getPid(), p.getBurstTime());
            initEvents.add(p.getPid().toLowerCase() + " a/s");
        }
        maxNj = Math.max(maxNj, active.size());
        rows.add(new String[]{fmt(time), String.join(", ", initEvents), "0", "0", "0", "0", formatLeft(active, left)});

        while (!active.isEmpty() || !pending.isEmpty()) {
            if (active.isEmpty()) {
                time = pending.get(0).getArrivalTime();
                List<String> ev = new ArrayList<>();
                while (!pending.isEmpty() && Math.abs(pending.get(0).getArrivalTime() - time) < EPS) {
                    ProgramProcess p = pending.remove(0);
                    active.add(p);
                    left.put(p.getPid(), p.getBurstTime());
                    ev.add(p.getPid().toLowerCase() + " a/s");
                }
                maxNj = Math.max(maxNj, active.size());
                rows.add(new String[]{fmt(time), String.join(", ", ev), "0", "0", "0", "0", formatLeft(active, left)});
                continue;
            }

            int nj = active.size();
            maxNj = Math.max(maxNj, nj);
            double cpuShare = 1.0 / nj;
            double minLeft = active.stream().mapToDouble(p -> left.get(p.getPid())).min().orElse(0);
            double nextFinish = minLeft * nj;
            double nextArrive = pending.isEmpty() ? Double.POSITIVE_INFINITY : pending.get(0).getArrivalTime() - time;

            double et = Math.min(nextFinish, nextArrive);
            double hw = et * cpuShare;
            time += et;
            activeCpuTime += et;

            List<String> events = new ArrayList<>();
            List<ProgramProcess> snapshot = new ArrayList<>(active);

            Iterator<ProgramProcess> it = active.iterator();
            while (it.hasNext()) {
                ProgramProcess p = it.next();
                double rem = Math.max(0, left.get(p.getPid()) - hw);
                left.put(p.getPid(), rem);
                if (rem < EPS) {
                    left.put(p.getPid(), 0.0);
                    finishMap.put(p.getPid(), time);
                    events.add(p.getPid().toLowerCase() + " t");
                    it.remove();
                }
            }

            while (!pending.isEmpty() && Math.abs(pending.get(0).getArrivalTime() - time) < EPS) {
                ProgramProcess arr = pending.remove(0);
                active.add(arr);
                snapshot.add(arr);
                left.put(arr.getPid(), arr.getBurstTime());
                events.add(arr.getPid().toLowerCase() + " a/s");
            }

            rows.add(new String[]{
                    fmt(time), String.join(", ", events), String.valueOf(nj),
                    fmt(cpuShare), fmt(et), fmt(hw), formatLeft(snapshot, left)
            });
        }

        List<ProgramProcess> completedJobs = new ArrayList<>();
        double totalTurnaround = 0.0;
        double totalBurst = 0.0;
        for (ProgramProcess req : requests) {
            double fin = finishMap.getOrDefault(req.getPid(), time);
            double tat = fin - req.getArrivalTime();
            totalTurnaround += tat;
            totalBurst += req.getBurstTime();
            completedJobs.add(new ProgramProcess(req.getPid(), req.getArrivalTime(), req.getBurstTime(),
                    ProcessStatus.COMPLETED, round3(fin), round3(tat)));
        }

        double span = Math.max(EPS, time - startTime);
        double cpuUtil = Math.min(100.0, (activeCpuTime / span) * 100.0);
        double avgTat = totalTurnaround / requests.size();

        return new MultiprogrammingOutput(
                requests.size(), maxNj, round3(startTime), round3(time),
                round3(totalBurst), round3(cpuUtil), round3(avgTat), completedJobs, rows
        );
    }

    public List<String> validate(List<ProgramProcess> jobs) {
        List<String> errors = new ArrayList<>();
        if (jobs == null || jobs.isEmpty()) {
            errors.add("At least one job must be provided.");
            return errors;
        }
        for (ProgramProcess j : jobs) {
            if (j.getArrivalTime() < 0) errors.add(j.getPid() + ": Arrival time (ar) cannot be negative.");
            if (j.getBurstTime() <= 0) errors.add(j.getPid() + ": Burst time (bt) must be greater than 0.");
        }
        return errors;
    }

    private String formatLeft(List<ProgramProcess> list, Map<String, Double> left) {
        List<String> parts = new ArrayList<>();
        for (ProgramProcess p : list) {
            parts.add(p.getPid() + ": " + fmt(left.get(p.getPid())));
        }
        return String.join("  |  ", parts);
    }

    private String fmt(double v) {
        return String.format("%.3f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private double round3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}
