use serde::{Deserialize, Serialize};
use serde_json::Value;
use std::{
    collections::BTreeMap,
    fs,
    path::{Path, PathBuf},
};
use tauri::{AppHandle, Manager};

const DATA_DIR_NAME: &str = "fitboard";
const DATA_FILE_NAME: &str = "data.json";
const BACKUP_FILE_NAME: &str = "data.backup.json";
const DEFAULT_TRAINING_TYPES: [&str; 7] = ["胸", "肩", "背", "腿", "手臂", "有氧", "休息"];
const DEFAULT_SUPPLEMENTS: [&str; 3] = ["蛋白粉", "肌酸", "咖啡因"];

#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
struct DailyRecord {
    date: String,
    training_completed: bool,
    training_types: Vec<String>,
    note: String,
    supplements: BTreeMap<String, bool>,
    updated_at: String,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
struct DashboardData {
    version: u32,
    training_types_config: Vec<String>,
    supplements_config: Vec<String>,
    records_by_date: BTreeMap<String, DailyRecord>,
}

#[tauri::command]
fn load_dashboard_data(app: AppHandle) -> Result<DashboardData, String> {
    let data_dir = ensure_data_dir(&app)?;
    let data_path = data_dir.join(DATA_FILE_NAME);
    let backup_path = data_dir.join(BACKUP_FILE_NAME);

    if !data_path.exists() {
        let payload = default_dashboard_data();
        persist_dashboard_data(&data_path, &backup_path, &payload)?;
        return Ok(payload);
    }

    let content = fs::read_to_string(&data_path).map_err(|error| error.to_string())?;
    let payload = parse_dashboard_data(&content)?;
    persist_dashboard_data(&data_path, &backup_path, &payload)?;
    Ok(payload)
}

#[tauri::command]
fn save_dashboard_data(app: AppHandle, payload: DashboardData) -> Result<(), String> {
    let data_dir = ensure_data_dir(&app)?;
    let data_path = data_dir.join(DATA_FILE_NAME);
    let backup_path = data_dir.join(BACKUP_FILE_NAME);
    let normalized = normalize_dashboard_data(payload);

    persist_dashboard_data(&data_path, &backup_path, &normalized)
}

#[tauri::command]
fn export_dashboard_data(payload: DashboardData, file_path: String) -> Result<(), String> {
    let normalized = normalize_dashboard_data(payload);
    write_json_file(Path::new(&file_path), &normalized)
}

#[tauri::command]
fn import_dashboard_data(app: AppHandle, file_path: String) -> Result<DashboardData, String> {
    let content = fs::read_to_string(&file_path).map_err(|error| error.to_string())?;
    let payload = parse_dashboard_data(&content)?;
    let data_dir = ensure_data_dir(&app)?;
    let data_path = data_dir.join(DATA_FILE_NAME);
    let backup_path = data_dir.join(BACKUP_FILE_NAME);

    persist_dashboard_data(&data_path, &backup_path, &payload)?;

    Ok(payload)
}

fn ensure_data_dir(app: &AppHandle) -> Result<PathBuf, String> {
    let app_dir = app
        .path()
        .app_data_dir()
        .map_err(|error| error.to_string())?
        .join(DATA_DIR_NAME);

    fs::create_dir_all(&app_dir).map_err(|error| error.to_string())?;

    Ok(app_dir)
}

fn persist_dashboard_data(
    data_path: &Path,
    backup_path: &Path,
    payload: &DashboardData,
) -> Result<(), String> {
    if data_path.exists() {
        fs::copy(data_path, backup_path).map_err(|error| error.to_string())?;
    }

    write_json_file(data_path, payload)?;
    write_json_file(backup_path, payload)?;

    Ok(())
}

fn write_json_file(path: &Path, payload: &DashboardData) -> Result<(), String> {
    let content = serde_json::to_string_pretty(payload).map_err(|error| error.to_string())?;
    fs::write(path, content).map_err(|error| error.to_string())
}

fn parse_dashboard_data(content: &str) -> Result<DashboardData, String> {
    let value: Value = serde_json::from_str(content).map_err(|error| format!("JSON 格式错误: {error}"))?;
    let object = value
        .as_object()
        .ok_or_else(|| "JSON 根节点必须是对象".to_string())?;

    let version = object
        .get("version")
        .and_then(Value::as_u64)
        .unwrap_or(2) as u32;

    let raw_records = object
        .get("recordsByDate")
        .or_else(|| object.get("records"))
        .map(parse_records_map)
        .transpose()?
        .unwrap_or_default();

    let mut training_types_config = parse_string_list(
        object.get("trainingTypesConfig"),
        "trainingTypesConfig 必须是字符串数组",
        &DEFAULT_TRAINING_TYPES,
    )?;
    let mut supplements_config = parse_supplements_config(object.get("supplementsConfig"))?;

    for record in raw_records.values() {
        for training_type in &record.training_types {
            if !training_types_config.contains(training_type) {
                training_types_config.push(training_type.clone());
            }
        }
    }

    for record in raw_records.values() {
        for supplement_name in record.supplements.keys() {
            if !supplements_config.contains(supplement_name) {
                supplements_config.push(supplement_name.clone());
            }
        }
    }

    let normalized = normalize_dashboard_data(DashboardData {
        version,
        training_types_config,
        supplements_config,
        records_by_date: raw_records,
    });

    Ok(normalized)
}

fn parse_records_map(value: &Value) -> Result<BTreeMap<String, DailyRecord>, String> {
    let object = value
        .as_object()
        .ok_or_else(|| "recordsByDate 必须是对象".to_string())?;

    object
        .iter()
        .map(|(date, record_value)| parse_record(date, record_value).map(|record| (date.clone(), record)))
        .collect()
}

fn parse_record(date: &str, value: &Value) -> Result<DailyRecord, String> {
    let object = value
        .as_object()
        .ok_or_else(|| format!("{date} 的记录格式无效"))?;

    let training_completed = object
        .get("trainingCompleted")
        .or_else(|| object.get("workoutCompleted"))
        .and_then(Value::as_bool)
        .unwrap_or(false);

    let training_types = object
        .get("trainingTypes")
        .and_then(Value::as_array)
        .map(|items| {
            items
                .iter()
                .filter_map(Value::as_str)
                .map(|item| item.trim().to_string())
                .filter(|item| !item.is_empty())
                .collect::<Vec<_>>()
        })
        .unwrap_or_default();

    let note = object
        .get("note")
        .and_then(Value::as_str)
        .unwrap_or_default()
        .to_string();

    let updated_at = object
        .get("updatedAt")
        .and_then(Value::as_str)
        .unwrap_or_default()
        .to_string();

    let supplements = parse_supplements_map(object.get("supplements"), date)?;

    Ok(DailyRecord {
        date: date.to_string(),
        training_completed,
        training_types,
        note,
        supplements,
        updated_at,
    })
}

fn parse_supplements_map(
    value: Option<&Value>,
    date: &str,
) -> Result<BTreeMap<String, bool>, String> {
    let Some(supplements_value) = value else {
        return Ok(BTreeMap::new());
    };

    let object = supplements_value
        .as_object()
        .ok_or_else(|| format!("{date} 的 supplements 必须是对象"))?;

    Ok(object
        .iter()
        .filter_map(|(key, value)| {
            let trimmed = key.trim();
            if trimmed.is_empty() {
                None
            } else {
                Some((trimmed.to_string(), value.as_bool().unwrap_or(false)))
            }
        })
        .collect())
}

fn parse_string_list(
    value: Option<&Value>,
    error_message: &str,
    defaults: &[&str],
) -> Result<Vec<String>, String> {
    let Some(list_value) = value else {
        return Ok(defaults.iter().map(|item| item.to_string()).collect());
    };

    let array = list_value
        .as_array()
        .ok_or_else(|| error_message.to_string())?;

    let mut result: Vec<String> = array
        .iter()
        .filter_map(Value::as_str)
        .map(|item| item.trim().to_string())
        .filter(|item| !item.is_empty())
        .collect();

    result.dedup();

    if result.is_empty() {
        Ok(defaults.iter().map(|item| item.to_string()).collect())
    } else {
        Ok(result)
    }
}

fn parse_supplements_config(value: Option<&Value>) -> Result<Vec<String>, String> {
    parse_string_list(value, "supplementsConfig 必须是字符串数组", &DEFAULT_SUPPLEMENTS)
}

fn normalize_dashboard_data(payload: DashboardData) -> DashboardData {
    let mut training_types_config = payload
        .training_types_config
        .into_iter()
        .map(|item| item.trim().to_string())
        .filter(|item| !item.is_empty())
        .collect::<Vec<_>>();

    if training_types_config.is_empty() {
        training_types_config = DEFAULT_TRAINING_TYPES.iter().map(|item| item.to_string()).collect();
    }

    training_types_config.sort();
    training_types_config.dedup();

    let mut supplements_config = payload
        .supplements_config
        .into_iter()
        .map(|item| item.trim().to_string())
        .filter(|item| !item.is_empty())
        .collect::<Vec<_>>();

    if supplements_config.is_empty() {
        supplements_config = DEFAULT_SUPPLEMENTS.iter().map(|item| item.to_string()).collect();
    }

    supplements_config.sort();
    supplements_config.dedup();

    let mut records_by_date = BTreeMap::new();

    for (date, record) in payload.records_by_date {
        let mut supplements = BTreeMap::new();

        for supplement_name in &supplements_config {
            let value = record
                .supplements
                .get(supplement_name)
                .copied()
                .unwrap_or(false);
            supplements.insert(supplement_name.clone(), value);
        }

        records_by_date.insert(
            date.clone(),
            DailyRecord {
                date,
                training_completed: record.training_completed,
                training_types: record
                    .training_types
                    .into_iter()
                    .map(|item| item.trim().to_string())
                    .filter(|item| !item.is_empty())
                    .collect(),
                note: record.note,
                supplements,
                updated_at: record.updated_at,
            },
        );
    }

    DashboardData {
        version: 3,
        training_types_config,
        supplements_config,
        records_by_date,
    }
}

fn default_dashboard_data() -> DashboardData {
    DashboardData {
        version: 3,
        training_types_config: DEFAULT_TRAINING_TYPES.iter().map(|item| item.to_string()).collect(),
        supplements_config: DEFAULT_SUPPLEMENTS.iter().map(|item| item.to_string()).collect(),
        records_by_date: BTreeMap::new(),
    }
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![
            load_dashboard_data,
            save_dashboard_data,
            export_dashboard_data,
            import_dashboard_data
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
